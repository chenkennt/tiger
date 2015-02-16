package main;

import frame.virtualframe.VirtualMachine;

import java.sql.Time;

/**
 * Created by IntelliJ IDEA.
 * User: KenChen
 * Date: 2004-1-5
 * Time: 15:49:14
 * To change this template use Options | File Templates.
 */
public class Run {
	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("Please input filename.");
			return;
		}

		String filename = args[0];
		VirtualMachine vm = new VirtualMachine(0x10000, 0x10000, 0x10000);
		long btime = System.currentTimeMillis();
		System.out.println("Program started at " + (new Time(btime).toString()) + ".");
		vm.run(filename);
		long etime = System.currentTimeMillis();
		System.out.println("Program terminated at " + (new Time(etime).toString()) + ".");
		System.out.println("Time elapsed: " + (etime - btime) + "ms.");
	}
}