
public class Vector {

	private int dimension;
	private double[] array;

	public Vector(int dim) {
		if (dim < 0) throw new IllegalArgumentException("Dimension less than zero");
		this.dimension = dim;
		this.array = new double[dim];
	}

	public void setValue(int index, double value) {
		if (index < 0 || index >= array.length)
			throw new IllegalArgumentException("Invalid dimension index");

		array[index] = value;
	}

	public int getDimension() {
		return this.dimension;
	}

	public double getValue(int index) {
		if (index < 0 || index >= array.length)
			throw new IllegalArgumentException("Invalid dimension index");

		return array[index];
	}

	public void print() {

		System.out.print("( ");
		for (int i = 0; i < dimension; i++) {
			if (i != dimension - 1)
				System.out.printf("%.2f, ", array[i]);
			else
				System.out.printf("%.2f )\n", array[i]);
		}
	}

	public void normalize() {
		double vectorLengthSquared = 0.0;
		for (int i = 0; i < array.length; i++) {
			vectorLengthSquared += array[i] * array[i];
		}
		double vectorLength = Math.sqrt(vectorLengthSquared);
		for (int i = 0; i < array.length; i++) {
			array[i] = array[i] / vectorLength;
		}
	}

	public double dotProduct(Vector that) {
		if (that.dimension != this.dimension)
			throw new IllegalArgumentException("Vectors have different sizes");

		double product = 0.0;
		for (int i = 0; i < dimension; i++)
			product += (that.getValue(i) * array[i]);

		return product;
	}
}