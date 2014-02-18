package dk.aau.cs.giraf.pictosearch;

import dk.aau.cs.giraf.pictogram.Pictogram;
/**
 * @author SW605f13 Parrot-group
 * A temporary class, used to sort the pictograms in the searchlist. Pictograms does not include a value, so this
 * was made as an alternative
 */
public class SearchNode {
	Pictogram pic;
	int searchvalue;
	
	public SearchNode(Pictogram p, int value){
		this.pic = p;
		this.searchvalue = value;
	}
}
