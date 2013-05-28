package cloudgene.mapred.steps;

import java.io.File;
import java.io.IOException;

import cloudgene.mapred.jobs.CloudgeneContext;
import cloudgene.mapred.jobs.CloudgeneStep;
import cloudgene.mapred.util.FileUtil;
import cloudgene.mapred.util.HdfsUtil;
import cloudgene.mapred.util.RMarkdown;
import cloudgene.mapred.util.rscript.MyRScript;
import cloudgene.mapred.util.rscript.RScript;

public class RScriptStep extends CloudgeneStep {

	@Override
	public boolean run(CloudgeneContext context) {

		String wd = context.getConfig().getPath();

		String rmd = context.getStep().getRmd();
		String output = context.resolveParams(context.getStep().getOutput());
		String paramsString = context.getStep().getParams();

		String[] params = context.resolveParams(paramsString.split(" "));

		context.println("Running script " + context.getStep().getRmd() + "...");
		context.println("Working Directory: " + wd);
		context.println("Output: " + output);
		context.println("Parameters:");
		for (String param : params) {
			context.println("  " + param);
		}

		convert(FileUtil.path(wd, rmd), output, params, context);
		return true;

	}

	public int convert(String rdmScript, String outputHtml, String[] args,
			CloudgeneContext context) {

		context.println("Creating RMarkdown report from " + rdmScript + "...");

		String name = rdmScript.replace(".Rmd", "");

		outputHtml = new File(outputHtml).getAbsolutePath();

		String folder = new File(outputHtml).getParentFile().getAbsolutePath()
				+ "/figures-temp/";

		MyRScript script = new MyRScript("convert.R");
		script.append("library(knitr)");
		script.append("opts_chunk$set(fig.path='" + folder + "')");
		script.append("library(markdown)");
		script.append("knit(\"" + rdmScript + "\", \"" + outputHtml + ".md\")");
		script.append("markdownToHTML(\"" + outputHtml + ".md\", \""
				+ outputHtml + "\")");
		script.save();

		RScript rScript = new RScript();
		rScript.setSilent(false);

		String[] argsForScript = new String[args.length + 1];
		argsForScript[0] = "convert.R";
		//argsForScript[1] = "--args";
		for (int i = 0; i < args.length; i++) {

			// checkout hdfs file
			if (args[i].startsWith("hdfs://")) {

				String localFile = new File(outputHtml).getParentFile()
						.getAbsolutePath() + "/local_file_" + i;
				context.println(localFile);
				try {
					HdfsUtil.checkOut(args[i], localFile);
					argsForScript[i + 1] = localFile;
				} catch (IOException e) {
					context.println(e.getMessage());
					argsForScript[i + 1] = args[i];
				}

			} else {

				argsForScript[i + 1] = args[i];

			}
		}

		rScript.setParams(argsForScript);
		int result = rScript.execute();

		new File(outputHtml + ".md").delete();
		new File("convert.R").delete();
		RMarkdown.deleteFolder(new File(folder));

		return result;

	}

	public static void deleteFolder(File folder) {
		File[] files = folder.listFiles();
		if (files != null) { // some JVMs return null for empty dirs
			for (File f : files) {
				if (f.isDirectory()) {
					deleteFolder(f);
				} else {
					f.delete();
				}
			}
		}
		folder.delete();
	}

}