import java.util.Comparator;
public class WarrantComparator implements Comparator<Warrant>{
	private int type;// 0 call by priceDiff increase then put, 1 only by priceDiff, 2 no sort
	public WarrantComparator() {type = 0;}
	public WarrantComparator(int t) {type = t;}

	public int compare(Warrant a, Warrant b) {
		switch(type) {
			case 0: return 0;
			case 1: return compare1(a, b);
			case 2: return compare2(a, b);
			case 3: return compare3(a, b);
			default: return 0;
		}
	}

	public int compare1(Warrant a, Warrant b){
		if(!a.isUpdated) return 1;
		if(!b.isUpdated) return -1;
		if(a.isCall)
			if(b.isCall)
				if(a.priceDiff < b.priceDiff) return -1;
				else return 1;
			else
				return -1;
		else
			if(b.isCall)
				return 1;
			else
				if(a.priceDiff < b.priceDiff) return -1;
				else return 1;
	}

	public int compare2(Warrant a, Warrant b){
		if(!a.isUpdated) return 1;
		if(!b.isUpdated) return -1;
		if(a.priceDiff < b.priceDiff) return -1;
		else return 1;
	}

	public int compare3(Warrant a, Warrant b){
		if(!a.isUpdated && !b.isUpdated) return 0;
		if(!a.isUpdated) return 1;
		if(!b.isUpdated) return -1;
		if(a.unchangeGain < b.unchangeGain)
			return 1;
		else if(a.unchangeGain == b.unchangeGain)
			return 0;
		else
			return -1;
	}
}
