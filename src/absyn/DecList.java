package absyn;

import symbol.Symbol;

public class DecList {
	public Dec head;
	public DecList tail;

	public DecList(Dec h, DecList t) {
		head = h;
		tail = t;
		rebuild();
	}

	private void rebuild() {
		if (head instanceof TypeDec) {
			if (tail != null && tail.head instanceof TypeDec) {
				((TypeDec)head).next = (TypeDec)tail.head;
				tail = tail.tail;
			}
		}
		else if (head instanceof FunctionDec) {
			if (tail != null && tail.head instanceof FunctionDec) {
				((FunctionDec)head).next = (FunctionDec)tail.head;
				tail = tail.tail;
			}
		}
	}
}