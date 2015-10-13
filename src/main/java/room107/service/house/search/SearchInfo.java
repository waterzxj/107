package room107.service.house.search;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import room107.dao.house.HouseResult;
import room107.datamodel.Location;

/**
 * @author LuoSZ
 */
@AllArgsConstructor
public class SearchInfo {
	
	@Setter@Getter
	private List<HouseResult> houseResults;
		
	@Setter@Getter
	private List<Location> queryLocations;
	
	public SearchInfo() {
		this.houseResults = new ArrayList<HouseResult>();
		this.queryLocations = new ArrayList<Location>();
	}
	
}
