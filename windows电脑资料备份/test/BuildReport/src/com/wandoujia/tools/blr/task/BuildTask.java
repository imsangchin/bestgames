package com.wandoujia.tools.blr.task;

public class BuildTask extends SimpleTask
{
	private static GitComment git;
	@Override
	public void execute() {
		// TODO Auto-generated method stub
		super.execute();
		System.out.println("start");
		git = new GitComment();
		git.getComment("a.txt");
	}
	
	public static void main(String[] args)
	{
		git = new GitComment();
		git.getComment("a.txt");
	}
	
}