package frame;

import translate.Translate;

import java.io.PrintStream;

/**
 * Created by IntelliJ IDEA.
 * User: KenChen
 * Date: 2004-1-4
 * Time: 16:35:38
 * To change this template use Options | File Templates.
 */
public abstract class AssemblyCodeGenerator {
	protected PrintStream out;

	public abstract void generate(Translate t);

	public AssemblyCodeGenerator(PrintStream o) {
		out = o;
	}
}