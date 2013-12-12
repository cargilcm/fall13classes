/*
 * jadvisor/school/COFC.java - SchoolAdapter for College of Charleston.
 * jadvisor.sourceforge.net
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 * CofC.java Authored by Chris Cargile - 9/23/13
 */

package jadvisor.school;

import jadvisor.advisorui.SwingWorker;
import jadvisor.scheduler.*;
import jadvisor.planner.*;

import java.util.*;
import java.io.*;
import java.net.*;

import javax.swing.*;

/**
 * This class implements <code>SchoolAdapter</code> for 
 * College of Charleston.
 *
 * @author Chris Cargile
 */
public class CofC implements SchoolAdapter {
	private static String CLASSES_URL = "http://catalogs.cofc.edu/undergraduate/computer-science-bs-major-requirements.htm";
	private static String COURSES_URL = "http://catalogs.cofc.edu/undergraduate/computer-science-bs-major-requirements.htm";
	private static String[] SEMESTER_NAMES_URL = 
	new String[] {"2013 Fall" , "2013 Summer" , "2013 Spring" , "2012 Fall" , 
	"2012 Summer" , "2012 Spring" , "2011 Fall" , "2011 Summer" , "2011 Spring" , 
	"2010 Fall" , "2009 Fall"
	};//  "https://ssb.cofc.edu:9710/prod/bwckschd.p_disp_dyn_sched";
	private static int DAYS_OLD = 5;
	
	private String[][] _departments;
	private String[][] _courses;
	private List[][] _classes;  // all the classes for a course ##
						// ie: ["110"}[StudentClass(.."BIOL110"..),StudentClass(.."CSCI110"..)]
	
	public static final int FALL = 0;
	public static final int SPRING = 1;
	public static final int SUMMER1 = 2;
	public static final int SUMMER2 = 3;
	public final String[] getSemesters () {
		return new String[] {"Fall", "Spring", "Summer 1", "Summer 2"};
	}
	
	public final boolean[] getRequiredSemesters () {
		return new boolean[] {true, true, false, false};
	}
	public final String getOptionalSemesterText () {return "Summers";}
	
	public static final int getDaysInWeek () {return 5;}
	public final String[] getDaysAbbreviations () {
		return new String[] {"M", "T", "W", "H", "F"};
	}
	
	public final String[] getClassTitles () {
		return new String[] 
		{"Pre", "Number", "Section", "Days", "Start Time", "End Time", "Mod", "Credit"};
	}
	public final String[] getClassInfoTitles () {
		return new String[] {"Teacher", "Building"};
	}

	public static final int NONE = 0;
	public static final int CREDIT = 1;
	public static final int AUDIT = 2;
	public final String[] getClassMods () {
		return new String[] {"None", "Credit", "Audit"};
	}
	
public CofC () {
	_departments = new String[][] {
		{"CSCI","MATH","PHYS","GEOL","CHEM",}
	};
	_courses = new String[_departments[0].length][];
	_classes = new List[_departments[0].length][];
}
/**
 * as implemented in DefaultSchoolAdapter, returned value will be a list all the 
 * departments. More helpfully, the system could benefit from a function that returns
 * the true values of courses for a given semester. Would this be implemented via:
 * a lookup against an ArrayList [semesters][courses] object?
 */
public String[] getClassPreList (int semester) {
	return _departments[0];
}

public String[] getClassNumberList (int semester, String pre) throws IOException {
	int i;
	for (i = 0; i < _departments[0].length && !_departments[0][i].equals(pre); i++) {}
	if (i >= _departments[0].length)
		return new String[0];

	if (_courses[i] == null) {
		try {
			getData(pre, SEMESTER_NAMES_URL[semester], _courses[i], _classes[i], i);
		} catch (Exception e) {
			//System.out.println("Get Data:" + e.getMessage());
			throw new IOException(e.getMessage());
		}
	}

	return _courses[i];
}

public List getClassSectionList (int semester, String pre, String number) 
		throws IOException {
	int i,j;
	getClassNumberList(semester, pre);

	for (i = 0; i < _departments[0].length && !_departments[0][i].equals(pre); i++) {}
	if (i >= _departments[0].length)
		throw new IOException("COFC: Could not find department \"" + pre + "\"");

	for (j = 0; j < _courses[i].length && !_courses[i][j].equals(number); j++) {}
	if (j >= _courses[i].length)
		throw new IOException("COFC: Could not find course \"" + pre + " " + number + "\"");
	System.out.println(((StudentClass)(_classes[i][j]).get(0)).getCourse().getCourseNumber()+" " + _classes[i][j]);
	return _classes[i][j];
}

public List getClassInfo (StudentClass c) {
	List l = new ArrayList();
	l.add("Professor Name");
	l.add("Building");
	return l;
}

public boolean isACourse (Course course) {
	//Substitute all this with call to server to check if course is there
	
	if (course.getCoursePre().length() > 3 || course.getCoursePre().length() < 2)
		return false;

	if (course.getCourseNumber().length() > 4 || course.getCourseNumber().length() < 3)
		return false;

	return true;
}

public boolean isAClass (StudentClass c) {
	//Substitute all this with call to server to check if class is there
	
	return true;
}

public List getCoursePrerequisites (Course course) {
	return new ArrayList();
}

public Curriculum getCurriculum (String curriculumName) {
	final Curriculum result = new Curriculum(curriculumName);

	String[] courses;
	try {
		courses = getClassNumberList(0, curriculumName);
	} catch (IOException e) {
		courses = new String[0];
	}
	if (courses != null)
		for (int i = 0; i < courses.length; i++)
			result.add(new Course(curriculumName, courses[i]));

	return result;
}

public String getCourseDescription (Course course) {
	return "";
}

public List searchCourses (String searchString, boolean searchTitles, 
		boolean searchDescriptions) {
	List a = new ArrayList();
	for (int i = 0; i < _classes.length; i++) {
		if(_classes[i]!=null)
			for(int j=0 ; j < _classes[i].length;j++){
				List classline = _classes[i][j];
				for(Object o:classline){
					StudentClass c = (StudentClass)o;
					String s = c.toString();
					if(s.contains(searchString))
						System.out.println(searchString +" matched: "+ c);
					}
			}
	}
	return a;
}

public String classToString (StudentClass c) {
	StringBuffer days = new StringBuffer();
		for (int i = 0; i < getDaysAbbreviations().length; i++)
			if(c.getDays()[i])
				days.append(getDaysAbbreviations()[i]);
			else
				days.append(" ");
	return c.getCourse() + " " + c.getSection() + " " + " "
		+ days + " " + timeToString(c.getStartTime()) + "-"
		+ timeToString(c.getEndTime()) + " " + c.getCredit();
}

public String timeToString (TimeOfDay time) {
	return time.toStringAMPM();
}

public String toString () {
	return "CofC SchoolAdapter";
}

private StudentClass parseClassData (String s1, String s2, String s3, String s6) {
	String[] tokens = new String[17];
	if(s2.contains("+"))
		s2=s2.replace("+", "");
    tokens[0] = s2;						//0 - COURSE PRE
	tokens[1] = s1	;					//1 - COURSE NUMBER
	
	tokens[2] = s3;						//2 - COURSE NAME
	tokens[3] = "002";					//3 - SECTION
	tokens[4] = "216320";				//4 - CALL NUMBER
	tokens[5] = "OPEN";					//5 - OPEN/RESTRICTED
	tokens[6] = s6;						//6 - CREDIT
	tokens[7] = "20";					//7 - CLASS SIZE
	tokens[8] = "10";					//8 - OPEN SEATS AVAILABLE

	tokens[9] = "M"	;					//9 - DAYS
	tokens[10] = "0910-1000"	;		//10 - TIME
	String altTime1 = "1100-1200"	;	//10 - TIME
	String altTime2 = "1300-1400";
	
	tokens[11] = "rudolph";				//11 - TEACHER
	tokens[12] = "harrelson";			//12 - BUILDING
	tokens[13] = "02132";				//13 - ROOM NUMBER
	tokens[14] = "0"	;				//14 - RESTR SEATS AVAILABLE
	tokens[15] = "N/A";					//15 - WAIT LIST AVAILABLE
	tokens[16] = "" ; 					//16- RESTRICTIONS

	boolean [] days = new boolean[getDaysAbbreviations().length];
	Arrays.fill(days, false);
	for (int i = 0; i < days.length; i++)
		if (tokens[9].indexOf(getDaysAbbreviations()[i]) != -1)
			days[i] = true;

	
	TimeOfDay[] times = parseTime(tokens[10]);//,parseTime(altTime1),parseTime(altTime2)};
    int credit;
	if (tokens[6].equals("var"))
		credit = 0;
	else
		credit = (int)Double.parseDouble(tokens[6]+".0");

	StudentClass result = new StudentClass(tokens[3],
		new Course(tokens[0], tokens[1]), tokens[2],	
		days, times[0], times[1], 0, credit);
	List classInfo = new ArrayList();
	classInfo.add(tokens[11]);		//11 - TEACHER
	classInfo.add("Harrelson");		//12 - BUILDING
	result.setInfo(classInfo);
	return result;
}








private TimeOfDay[] parseTime (String s) {
	if (s.equals(""))
		return new TimeOfDay[] {new TimeOfDay(12,00), new TimeOfDay(12,00)};
	TimeOfDay[] result = new TimeOfDay[2];
	String[] s1 = new String[2];
	StringTokenizer t = new StringTokenizer(s, "-");
	for (int i = 0; t.hasMoreTokens(); i++)
		s1[i] = t.nextToken();
	
	int starth = Integer.parseInt(s1[0].substring(0,2));
	int startm = Integer.parseInt(s1[0].substring(2,4));
	int endh = Integer.parseInt(s1[1].substring(0,2));
	int endm = Integer.parseInt(s1[1].substring(2,4));
	
	if (s1[1].length() == 6 && starth < 12)
		starth += 12;
	if (s1[1].length() == 6)
		endh += 12;

	result[0] = new TimeOfDay(starth, startm);
	result[1] = new TimeOfDay(endh, endm);
	return result;
}

private void makeDataCache (InputStream i, File f) throws IOException {
	BufferedWriter out = new BufferedWriter(new FileWriter(f));
	BufferedInputStream bin = new BufferedInputStream(i);
	int c;
	while ((c = bin.read()) != -1)
		out.write(c);

	bin.close();
	out.close();
}

//adapted from JCarnegie (carnegie.handlers.ASU) by Mark Edgar
private void getData (String pre, String semester, String[] courses, List[] classes, int k) 
		throws IOException {
	File tmpDir = getTmpDir();
	File cacheFile = new File(tmpDir, "jadvisor-COFC-" + semester + "-" + pre + ".cache");
	if (tmpDir != null && !cleanCache(cacheFile)) {
		if (cacheFile.exists()) {
			System.err.println("DEBUG: Got cacheFile == " + cacheFile);
			try {
//				System.err.println("DEBUG: Parsing Class Data");
				readData(cacheFile, courses, classes, k);
//				System.err.println("DEBUG: Parsing Done");
				makeDataCacheThreaded(pre, semester, cacheFile);
				return;
			} catch (IOException e) {
				System.err.println("Could not read cache file: " + e.getMessage());
				removeCacheFile(cacheFile);
			}
		} else {
			System.err.println("DEBUG: " + cacheFile + ": Cache file does not exist");
		}
	}

	URL u = new URL(CLASSES_URL + "?"+semester + "/courses/" + pre + ".html");
	URLConnection c = u.openConnection();
	if (cacheFile == null)
		c.setUseCaches(true);
	
	System.err.println("DEBUG: Fetching URL: " + u);
	try {
		makeDataCache(c.getInputStream(), cacheFile);
	} catch (IOException e) {
		removeCacheFile(cacheFile);
		System.err.println("Could not make cache file: " + cacheFile.toString()+", "+e.getMessage());
		throw new IOException("COFC could not access " + u);
	}
	System.err.println("DEBUG: Parsing Class Data");
	try {
		readData(cacheFile, courses, classes, k);
	} catch (IOException e) {
		removeCacheFile(cacheFile);
		System.err.println("Could not read cache file: " + e.getMessage());
		throw new IOException("Could not read cache file: " + e.getMessage());
	}
	System.err.println("DEBUG: Parsing Done");
}

//returns true if cache file is more than DAYS_OLD days old.
private boolean cleanCache(File cacheFile) {
	long numdaysold = System.currentTimeMillis() - 24*60*60*1000*DAYS_OLD;
	return cacheFile.lastModified() < numdaysold;
}

private File getTmpDir() {
	File f;
	try {
		f = File.createTempFile("foo", null);
	} catch (IOException e) {
		System.err.println("DEBUG: Could not create temporary file: " + e.getMessage());
		return null;
	}
	f.delete();
	return f.getParentFile();
}

private void removeCacheFile (File cacheFile) {
	cacheFile.delete();
}

private void readData(File f, String[] courses, List[] classes, int k) throws IOException  {
	BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
	String line, line2, line3="", dept = "", course = "", courseTitle="", credit="";
	StudentClass c;
	List courseList = new LinkedList();
	List classList = new LinkedList();
	
	int j=0;
	try {
		while ((line = reader.readLine()) != null){
			if(line.length()>=76 && line.contains("<p class=\"courses\"><a")){
				line=line.substring(75,line.length());
				dept=line.substring(0,4);
				course=line.substring(5,9);
				course=course.trim();
				if(line.contains("href=")){
					int ctEnd=line.indexOf(" href=");
					courseTitle=line.substring(0,ctEnd-1);
					String prefix=dept + " " + course;
					courseTitle=courseTitle.replace(prefix+" ", "");
					credit=courseTitle.substring(courseTitle.length()-2,courseTitle.length()-1);
					courseTitle=courseTitle.substring(0,courseTitle.length()-4);
					System.out.println(prefix+","+courseTitle+","+credit);
				}
				c = parseClassData(course, dept, courseTitle, credit);
				TimeOfDay[] times = parseTime("1100-1200");
				TimeOfDay[] times2 = parseTime("1200-1300");
				TimeOfDay[] times3 = parseTime("1400-1500");
				
				TimeOfDay start1 = times[0];
				TimeOfDay end1 = times[1];
				TimeOfDay start2 = times2[0];
				TimeOfDay end2 = times2[1];

				TimeOfDay start3 = times3[0];
				TimeOfDay end3 = times3[1];
				StudentClass alt1=new StudentClass(c);
				StudentClass alt2=new StudentClass(c);
				StudentClass alt3=new StudentClass(c);
				alt1.setTime(start1, end1);
				alt2.setTime(start2, end2);
				alt3.setTime(start3, end3);
				//StudentClass c2 = alt1.getStudentClass();
				if (!courseList.contains(c.getCourse().getCourseNumber())){
					courseList.add(new String(c.getCourse().getCourseNumber()));
			//		courseList.add(new String(c.getCourse().getCourseNumber()));}
			}
				classList.add(c);
				classList.add(alt1);
				classList.add(alt2);
				classList.add(alt3);
				}
		}
	} catch (Exception e) {
		throw new IOException("Invalid cache file1: " + f);
	}
	
	reader.close();
	// _courses[] and _classes[] are pretty similar in terms of their
	// backing content, they differ on the basis of being sorted and 
	// the data structure that implements each.
	Object[] c2 = courseList.toArray();
	_courses[k] = new String[c2.length];
	for (int i = 0; i < c2.length; i++) {
		_courses[k][i] = (String)c2[i];
	}
	Arrays.sort(_courses[k]);

	_classes[k] = new List[_courses[k].length];
	for (int i = 0; i < _classes[k].length; i++)
		_classes[k][i] = new ArrayList();
		
	for (int i = 0; i < classList.size(); i++) {
		c = (StudentClass)classList.get(i);
		String s = c.getCourse().getCourseNumber();
		int n = Arrays.binarySearch(_courses[k], s);
		_classes[k][n].add(c);	
	}
	
}

private void makeDataCacheThreaded (final String pre, final String semester, 
		final File cacheFile) {
	final SwingWorker worker = new SwingWorker() {
		public Object construct() {
			long numdaysold = System.currentTimeMillis() - 24*60*60*1000;
			if (cacheFile.lastModified() < numdaysold) {
				try {
				URL u = new URL(CLASSES_URL + semester + "/courses/" + pre + ".html");
				URLConnection c = u.openConnection();
				if (cacheFile == null)
					c.setUseCaches(true);

				System.err.println("DEBUG: Thread Fetching URL: " + u);
				makeDataCache(c.getInputStream(), cacheFile);
				System.err.println("DEBUG: Thread Done");
				} catch (IOException e) {}
			}
			return null;
		}
	};
	worker.start();
}

}
