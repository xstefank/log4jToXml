package log4JToXml.propertiesToXml;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by FH on 7.5.2015.
 * Represents a line in a log4j properties file, parsed for easier manipulation
 * e.g.:
 * log4j.com.class.name = foo
 * becomes a Package with 4 levels: log4j, com, class, name and a value of foo
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

	/**
	 * @param from
	 * @param to
	 * @return A dot separated string containing the corresponding levels, inclusive
	 */
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

	/**
	 *
	 * @param from
	 * @return A dot separated string containg the corresponding levels up to the last, inclusive
	 */
	public String groupLevelsFrom(int from)
	{
		if (from < 0)
		{
			throw new IllegalArgumentException("Invalid parameters.");
		}
		return levels.stream()
				.skip(from)
				.collect(Collectors.joining("."));
	}

	/**
	 *
	 * @return the last level of the property
	 */
	public String getLastLevel()
	{
		return getLevel(numLevels() - 1);
	}

	/**
	 *
	 * @param i
	 * @return the ith level of the property, zero-indexed
	 */
	public String getLevel(int i)
	{
		return levels.get(i);
	}

	/**
	 *
	 * @return the number of levels the property has
	 */
	public int numLevels()
	{
		return levels.size();
	}

	/**
	 *
	 * @return the value of the log4j property
	 */
	public String getValue()
	{
		return this.value;
	}
}
