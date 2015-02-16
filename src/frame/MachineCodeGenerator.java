package frame;

import java.io.DataOutputStream;
import java.io.BufferedReader;

/**
 * Created by IntelliJ IDEA.
 * User: KenChen
 * Date: 2004-1-4
 * Time: 17:26:03
 * To change this template use Options | File Templates.
 */
public abstract class MachineCodeGenerator {
	protected DataOutputStream out;
	protected BufferedReader in;

	public abstract void generate();

	public MachineCodeGenerator(BufferedReader i, DataOutputStream o) {
		in = i;
		out = o;
	}
}