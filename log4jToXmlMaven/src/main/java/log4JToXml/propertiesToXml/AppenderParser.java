package log4JToXml.propertiesToXml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by FH on 7.5.2015.
 */
public class AppenderParser
{
	private Document doc;
	private Element appender;
	private List<Package> all;

	/**
	 *
	 * @param lines properties relevant to the current appender
	 * @param all all of the properties, because some properties out of the scope of the appender are required
	 * @param doc the Document we are building
	 */
	public void parse(List<Package> lines, List<Package> all, Document doc)
	{
		String appenderName = lines.get(0).getLevel(2);
		this.doc = doc;
		this.all = all;

		Element appenderElem = doc.createElement("appender");
		this.appender = appenderElem;
		try
		{
		Package appenderType = lines.stream()
				.filter(o -> o.numLevels() == 3)
				.findFirst()
				.get();

		appender.setAttribute("class", appenderType.getValue());
		
		
		appender.setAttribute("name", appenderName);
		lines.remove(appenderType);
		}
		catch(NoSuchElementException ex)
		{
			throw new IllegalArgumentException("The properties file is invalid (appender has no class defined)", ex);
		}
		for (Package line : lines)
		{
			//first the special cases
			//basically, if it has one subpackage, it's a simple param, if not, it's something nested.
			//we are now in log4j.appender.appenderName, so 3 deep
			if (line.numLevels() == 4 && line.getLevel(3).equals("layout"))
			{
				handleLayout(line, lines);
			}
			else if (line.numLevels() >= 4 && line.getLevel(3).equals("RollingPolicy"))
			{
				handleRollingPolicy(line);
			}
			else if(line.getLevel(3).equals("appender-ref"))
			{
				String[] appenderRefs = line.getValue().split(",");
					for(String appenderRef : appenderRefs)
					{
						Element appenderRefElement = doc.createElement("appender-ref");
						appenderRefElement.setAttribute("ref", appenderRef.trim());
						appenderElem.appendChild(appenderRefElement);
					}
			}
			else if (line.numLevels() == 4 && !line.getLevel(3).equals("errorhandler"))
			{
				Element element = doc.createElement("param");
				element.setAttribute("name", line.getLevel(3));
				element.setAttribute("value", line.getValue());
				appenderElem.appendChild(element);
			}
		}
		handleFilter(lines);
		handleErrorHandler(lines);
		doc.getDocumentElement().appendChild(appenderElem);
	}

	private void handleRollingPolicy(Package policyLine)
	{
//		log4j.appender.LOGFILE.RollingPolicy=org.apache.log4j.rolling.TimeBasedRollingPolicy
//      log4j.appender.LOGFILE.RollingPolicy.FileNamePattern=/opt/zimbra/log/mailbox.log.%d{yyyy-MM-dd}.gz
		Element element = doc.createElement("param");
		element.setAttribute("name", policyLine.getLastLevel());
		element.setAttribute("value", policyLine.getValue());
		appender.appendChild(element);
	}

	private void handleErrorHandler(List<Package> lines)
	{
		//	log4j.appender.appenderName.errorhandler=fully.qualified.name.of.filter.class
		//	log4j.appender.appenderName.errorhandler.param=value
		/*
		log4j.appender.file.errorhandler=org.apache.log4j.varia.FallbackErrorHandler
		log4j.appender.file.errorhandler.root-ref=true
		log4j.appender.file.errorhandler.logger-ref=com.foo, com.fooHaa
		log4j.appender.file.errorhandler.appender-ref=stdout
		*/
		List<Package> errorHandlerLines = lines.stream().filter(o -> o.getLevel(3).equals("errorhandler")).collect(Collectors.toList());
		try
		{
			//get the fully qualified name
			Package fqnLine = errorHandlerLines.stream().filter(o -> o.numLevels() == 4).findFirst().get();
			errorHandlerLines.remove(fqnLine);

			Element errorHandlerElement = doc.createElement("errorHandler");
			errorHandlerElement.setAttribute("class", fqnLine.getValue());

			for (Package errorHandlerLine : errorHandlerLines)
			{
				String levelFour = errorHandlerLine.getLevel(4);
				if(levelFour.equals("root-ref"))
				{
					Element paramElement = doc.createElement("root-ref");
					errorHandlerElement.appendChild(paramElement);
				}
				else if(levelFour.equals("logger-ref") || levelFour.equals("appender-ref"))
				{
					String[] loggerRefs = errorHandlerLine.getValue().split(",");
					for(String loggerRef : loggerRefs)
					{
						Element loggerRefElement = doc.createElement(levelFour);
						loggerRefElement.setAttribute("ref", loggerRef.trim());
						errorHandlerElement.appendChild(loggerRefElement);
					}
				}
				else
				{
					Element paramElement = doc.createElement("param");
					paramElement.setAttribute("name", errorHandlerLine.getLevel(4));
					paramElement.setAttribute("value", errorHandlerLine.getValue());
					errorHandlerElement.appendChild(paramElement);
				}
			}
			appender.appendChild(errorHandlerElement);
		}
		catch (NoSuchElementException ex)
		{
			//thrown by fqnLine (get())
			//this is fine, there's just no error handler in the properties
		}
	}

	private void handleFilter(Collection<Package> lines)
	{
		Map<String, List<Package>> filterMap = lines.stream()
				.filter(o -> o.getLevel(3).equals("filter") && o.numLevels() > 4)
				// filters are labeled
				// log4j.appender.CONSOLE.filter.02
				.collect(Collectors.groupingBy(o -> o.getLevel(4)));

		for (Map.Entry<String, List<Package>> entry : filterMap.entrySet())
		{
			Element filterElement = doc.createElement("filter");
			Package classBearer = entry.getValue().stream().filter(o -> o.numLevels() == 5).findFirst().get();
			filterElement.setAttribute("class", classBearer.getValue());
			for (Package line : entry.getValue().stream().filter(o -> o.numLevels() == 6).collect(Collectors.toList()))
			{
				Element paramElement = doc.createElement("param");
				paramElement.setAttribute("name", line.getLevel(5));
				paramElement.setAttribute("value", line.getValue());
				filterElement.appendChild(paramElement);
			}
			appender.appendChild(filterElement);
		}
	}

	private void handleLayout(Package layoutHeader, List<Package> lines)
	{
		Element layoutElement = doc.createElement("layout");
		layoutElement.setAttribute("class", layoutHeader.getValue());
		List<Package> params = lines.stream()
				.filter(o -> o.numLevels() == 5 && o.getLevel(3).equals("layout"))
				.collect(Collectors.toList());
		//if there are things more than 5 levels deep, they are not covered in the implementation
		for (Package param : params)
		{
			Element paramElement = doc.createElement("param");
			paramElement.setAttribute("name", param.getLevel(4));
			paramElement.setAttribute("value", param.getValue());
			layoutElement.appendChild(paramElement);
		}
		appender.appendChild(layoutElement);
	}
}
