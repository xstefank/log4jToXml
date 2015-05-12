import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by FH on 7.5.2015.
 */
public class Package
{
	private List<String> levels = new ArrayList<>();
	private String value;

	public Package(String value, List<String> levels)
	{
		this.value = value;
		this.levels = levels;
	}

	public String groupLevels(int from, int to)
	{
		if (from < 0 || to > numLevels())
		{
			throw new IllegalArgumentException("Invalid parameters.");
		}
		return levels.stream()
				.filter(o -> levels.indexOf(o) >= from && levels.indexOf(o) <= to)
				.collect(Collectors.joining("."));
	}

	public String groupLevelsFrom(int from)
	{
		if (from < 0)
		{
			throw new IllegalArgumentException("Invalid parameters.");
		}
		return levels.stream()
				.filter(o -> levels.indexOf(o) >= from)
				.collect(Collectors.joining("."));
	}

	public String getLastLevel()
	{
		return getLevel(numLevels() - 1);
	}

	public String getLevel(int i)
	{
		return levels.get(i);
	}

	public int numLevels()
	{
		return levels.size();
	}

	public String getValue()
	{
		return this.value;
	}
}
