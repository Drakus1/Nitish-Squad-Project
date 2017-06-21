

public class Conversions {

	private int significantDigits;

	public Conversions(int sigDigits) {
		if (sigDigits < 1) 
			throw new IllegalArgumentException("Must have > 0 significant digits");
		this.significantDigits = sigDigits;
	}

	public int getSigDigits() {
		return this.significantDigits;
	}

	public int[] wholeNumberVector(Vector myVector) {

		int[] iVectorArray = new int[myVector.getDimension()];

		for (int index = 0; index < myVector.getDimension(); index++) {

			double currentValue = myVector.getValue(index);
			currentValue *= Math.pow(10, significantDigits);

			double dFloor = Math.floor(currentValue);
			int iFloor = (int) dFloor;

			iVectorArray[index] = iFloor;		
		}

		return iVectorArray;
	}

	public double scaledDotProduct(int dotProduct) {

		int shiftPoint = 2 * significantDigits;

		double scaled = dotProduct / Math.pow(10, shiftPoint);

		return scaled;
	}
}