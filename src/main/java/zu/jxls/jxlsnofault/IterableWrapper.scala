package zu.jxls.jxlsnofault

import java.util.Iterator
import java.util.List

import org.apache.commons.beanutils.PropertyUtils
import scala.collection.JavaConverters._

/**
 * This class wraps an object that JXLS could use to render an Excel.
 * Since it's an Iterable object, JXLS doesn't raise an exception if
 * some prop isn't valuable.
 * 
 * See JXLS GridCommand class used from SimpleExporter
 * https://bitbucket.org/leonate/jxls/src/65fb5b705a70a8d8d3971e2abb8ec665d47434eb/src/main/java/org/jxls/command/GridCommand.java?at=r2.4.3&fileviewer=file-view-default
 * 
 * At row 163 if the object is Iterable then the evaluation is more simple otherwise al line 190 PropertyUtils.getProperty() may throw an Exception
 * for instance when you can to access to a child object property when the child object is null.
 * see row 197 and 199 with its message: "Failed to evaluate property ...  of row object of class ..."
 *  
 * @author Ravinder, Luciano Zu
 *
 */
class IterableWrapper (data: Object, fieldNames: List[String]) extends java.lang.Iterable[String]() {

	override def iterator(): Iterator[String] = {
		new InnerIterator(data, fieldNames.iterator())
	}
	
	private class InnerIterator(data: Object, fieldNames: Iterator[String]) extends Iterator[String]() {

		override def hasNext(): Boolean = {
			fieldNames.hasNext()
		}

		override def next(): String = {
			val field = fieldNames.next()
			var value: String = null
			try {
				val fields = if (field.contains("."))field.split("\\.")  else Array(field)
				def getValue(a: Object, b: Array[String]): Object = {
					val r: Object = if (a != None && b.headOption.isDefined)
						getValue(a.getClass.getMethod(b.head).invoke(a), b.tail)
					else a
					r
				}
				
				value = getValue(data.asInstanceOf[Object], fields) match {
					case Some(a) => a.toString()
					case None => ""
					case a => a.toString()
				}
			} catch {
				 case e =>
					value = ""
			}
			return value;
		}		
	}
}
