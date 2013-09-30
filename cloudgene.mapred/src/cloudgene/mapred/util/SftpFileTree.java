package cloudgene.mapred.util;

import java.util.Vector;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import org.apache.commons.io.FileUtils;

public class SftpFileTree {
	
	public static FileItem[] getSftpFileTree(String path, String SFTPHOST, String SFTPUSER, String SFTPPASS, int SFTPPORT) {
					
		Session 	session 	= null;
		Channel 	channel 	= null;
		ChannelSftp channelSftp = null;
		try{
			JSch jsch = new JSch();
			session = jsch.getSession(SFTPUSER,SFTPHOST,SFTPPORT);
			session.setPassword(SFTPPASS);
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.connect();
			channel = session.openChannel("sftp");
			channel.connect();
			channelSftp = (ChannelSftp)channel;
			channelSftp.cd(path);
			if(path.equals("/")) {path = channelSftp.pwd();}
			Vector<ChannelSftp.LsEntry> filelist = channelSftp.ls(path);
			FileItem[] results = null;
			//-2 to take away folder ".." and "."
			results = new FileItem[filelist.size()-1];
			int count = 0;
			
			for(ChannelSftp.LsEntry entry : filelist) {
			 if(((ChannelSftp.LsEntry) channelSftp.ls(channelSftp.realpath(path + entry.getFilename())).get(0)).getAttrs().isDir()  && !(entry.getFilename().equals(".") ) ){
				 results[count] = new FileItem();
				 results[count].setId(channelSftp.realpath(path + entry.getFilename()));
				 results[count].setText(entry.getFilename());
				 results[count].setPath(channelSftp.realpath(path + entry.getFilename()));
				 results[count].setCls("folder");
				 results[count].setLeaf(false); 
				 count++;
			 }
			}
			
			 for(ChannelSftp.LsEntry entry : filelist) {
				 if(!((ChannelSftp.LsEntry) channelSftp.ls(channelSftp.realpath(path + entry.getFilename())).get(0)).getAttrs().isDir()){
					results[count] = new FileItem();
					results[count].setText(entry.getFilename());
					results[count].setPath(channelSftp.realpath(path + entry.getFilename()));
					results[count].setId(channelSftp.realpath(path + entry.getFilename()));
					results[count].setLeaf(true);
					results[count].setCls("file");
					results[count].setSize(FileUtils
							.byteCountToDisplaySize(entry.getAttrs().getSize()));
					count++;
				 }
			}
			return results;
			
		}catch(Exception ex){
			ex.printStackTrace();
			return null;
		}
		
		
	}

}
