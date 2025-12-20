package dev.stxt;

class ValidatorCountTest implements Validator {
	public int total = 0;

	@Override
	public void validate(Node n) {
		System.out.println("  => " + n.getQualifiedName());
		total++;
	}
}
