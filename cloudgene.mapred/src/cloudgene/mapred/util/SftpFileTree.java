package cloudgene.mapred.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SftpFileTree {
	 private static final Log log = LogFactory.getLog(SftpFileTree.class);
	@SuppressWarnings("unchecked")
	public static FileItem[] getSftpFileTree(String path, String SFTPHOST,
			String SFTPUSER, String SFTPPASS, int SFTPPORT)
			throws JSchException, SftpException, IOException {

		Session session = null;
		Channel channel = null;
		ChannelSftp channelSftp = null;

		JSch jsch = new JSch();
		session = jsch.getSession(SFTPUSER, SFTPHOST, SFTPPORT);

		session.setPassword(SFTPPASS);
		java.util.Properties config = new java.util.Properties();
		config.put("StrictHostKeyChecking", "no");
		session.setConfig(config);

		session.connect();
		//log.info("PATH IS " + path);
		
		FileItem[] results = null;

		if (path.equals("LISTMYPROJ12")) {
			ChannelExec channelE = (ChannelExec) session.openChannel("exec");
			((ChannelExec) channelE).setCommand("groups");
			channelE.setInputStream(null);
			((ChannelExec) channelE).setErrStream(System.err);
			InputStream in = channelE.getInputStream();
			channelE.connect();
			String StringFromInputStream = IOUtils.toString(in, "UTF-8").replace("\n", "");
			String[] groups = StringFromInputStream.split(" ");
			channelE.disconnect();
			int groupNR = 0;
			for (int i = 0; i < groups.length; i++) {
				if (groups[i].startsWith("b20")) {
					groupNR++;
				}
			}
			results = new FileItem[groupNR];
			int count = 0;
			for (int i = 0; i < groups.length; i++) {
				if (groups[i].startsWith("b20")) {
					results[count] = new FileItem();
					results[count].setText(groups[i]);
					results[count].setLeaf(false);
					results[count].setCls("folder");
					results[count].setId("/proj/" + groups[i]);
					results[count].setPath("/proj/" + groups[i]);
					count++;
				}
			}
			session.disconnect();
			return results;

		} else {
			channel = session.openChannel("sftp");
			channel.connect();
			channelSftp = (ChannelSftp) channel;
			if (path.equals("~/")) {
				path = channelSftp.pwd();

			}
			
			channelSftp.cd(path);

			Vector<LsEntry> filelist = null;
			filelist = channelSftp.ls(path);

			

			// -2 to take away folder ".." and "."
			results = new FileItem[filelist.size() - 2];
			int count = 0;
			for (ChannelSftp.LsEntry entry : filelist) {
				if (entry.getAttrs().isDir()
						&& !((entry.getFilename().equals(".") || (entry
								.getFilename().equals(".."))))) {
					results[count] = new FileItem();
					results[count].setText(entry.getFilename());
					results[count].setLeaf(false);
					results[count].setCls("folder");
					results[count].setId(path + "/" + entry.getFilename());
					results[count].setPath(path + "/" + entry.getFilename());
					count++;
				}
			}
			for (ChannelSftp.LsEntry entry : filelist) {

				if (entry.getAttrs().isLink()) {
					String link = null;
					boolean linkIsdir = false;
					link = channelSftp.readlink(entry.getFilename());
					try {
						linkIsdir = channelSftp.lstat(link).isDir();
					} catch (com.jcraft.jsch.SftpException ex) {
						if (ex.getMessage().equals("No such file")) {
							results[count] = new FileItem();
							results[count].setText(entry.getFilename()
									+ " BROKEN LINK");
							results[count].setLeaf(true);
							results[count].setCls("file");
							results[count].setDisabled(true);
							count++;
							continue;
						} else {
							ex.printStackTrace();
						}
					}
					if (linkIsdir) {
						results[count] = new FileItem();
						results[count].setText(entry.getFilename());
						results[count].setLeaf(false);
						results[count].setCls("folder");
						results[count].setId(path + "/" + entry.getFilename());
						results[count]
								.setPath(path + "/" + entry.getFilename());
						count++;
					} else {
						results[count] = new FileItem();
						results[count].setText(entry.getFilename());
						results[count].setPath(path + "/" + link);
						results[count].setId(path + "/" + link);
						results[count].setLeaf(true);
						results[count].setCls("file");
						results[count].setSize(FileUtils
								.byteCountToDisplaySize(entry.getAttrs()
										.getSize()));
						count++;
					}

				} else if (!entry.getAttrs().isDir()
						&& !((entry.getFilename().equals(".") || (entry
								.getFilename().equals(".."))))) {
					results[count] = new FileItem();
					results[count].setText(entry.getFilename());
					results[count].setPath(path + "/" + entry.getFilename());
					results[count].setId(path + "/" + entry.getFilename());
					results[count].setLeaf(true);
					results[count].setCls("file");
					results[count]
							.setSize(FileUtils.byteCountToDisplaySize(entry
									.getAttrs().getSize()));
					count++;
				}

			}

			channel.disconnect();
			session.disconnect();
			return results;

		}
	}

}
