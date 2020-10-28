import java.util.*;
import java.io.*;

public class ShiftScheduler
{
	// number of shifts on each day
	public static ArrayList<Integer> days;
	public static int nonDesignTeamExtra = 0;
	public static void main(String args[])
	{
		days = new ArrayList<Integer>();
		int totalSpots = 0;
		int shiftsScheduled = 0;
		int designTeamCount = 0;
		Scanner keyboard = new Scanner(System.in);
		
		System.out.print("Enter name of SHIFT input file: ");
		String shiftInputFile = keyboard.next();
		Scanner shiftInput = null;
		try
		{
			shiftInput = new Scanner(new File(shiftInputFile));
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
		
		ArrayList<Shift> shifts = new ArrayList<Shift>();
		while(shiftInput.hasNextLine())
		{
			String line = shiftInput.nextLine();
			String[] input = line.split("\\s+");
			int day = Integer.parseInt(input[0]); 
			int position = Integer.parseInt(input[1]);
			int capacity = Integer.parseInt(input[2]);
			if(position == 0)
				days.add(1);
			else
				days.set(day, position + 1);
			totalSpots += capacity;
			Shift s = new Shift(day, position, capacity);
			shifts.add(s);
		}
		
		System.out.print("Enter name of PERSON input file: ");
		String personInputFile = keyboard.next();
		Scanner personInput = null;
		try
		{
			personInput = new Scanner(new File(personInputFile));
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
		
		ArrayList<Person> persons = new ArrayList<Person>();
		while(personInput.hasNextLine())
		{
			String line = personInput.nextLine();
			String[] input = line.split("\\s+");
			String name = input[0];
			boolean designTeam = input[1].equals("true") ? true : false;
			if(designTeam)
				designTeamCount++;
			Person p = new Person(name, designTeam);
			for(int i = 2; i < input.length; i++)
			{
				if(input[i].equals("1"))
				{
					p.schedule(shifts.get(i-2));
					shifts.get(i-2).schedule(p);
					shiftsScheduled++;
				}
			}
			persons.add(p);
		}
		
		System.out.print("How many extra shifts would you like to give non-design team members? ");
		nonDesignTeamExtra = keyboard.nextInt();
		// how many shifts to give everyone (non-design team members will get more)
		int baseShifts = (totalSpots - nonDesignTeamExtra * (persons.size() - designTeamCount)) / persons.size() + 1;
		// then take away this many more to account for remainder
		int adjustment = persons.size() - (totalSpots - nonDesignTeamExtra * (persons.size() - designTeamCount)) % persons.size();
		System.out.println(baseShifts + " shifts for design team, " + (baseShifts+nonDesignTeamExtra) + " shifts for everyone else\n");
		
		// shuffle in case sort is stable
		Collections.shuffle(persons, new Random((int)(Math.random() * 100)));
		Collections.sort(persons);
		
		for(Person p : persons)
		{
			int target = baseShifts;
			if(!p.isDesignTeam())
				target += nonDesignTeamExtra;
			if(p.shiftCount() < target)
			{
				adjustment -= (target - p.shiftCount());
				continue;
			}
			while(p.shiftCount() > target)
			{
				Shift s = p.getRemove();
				if(s == null)
					throw new NullPointerException("Ran out of shifts to remove for " + p.getName());
				p.unschedule(s);
				s.unschedule(p);
			}
		}
		
		Collections.shuffle(persons, new Random((int)(Math.random() * 100)));
		Collections.sort(persons);
		
		int errorCounter = 0; // keeps track of how many failed attempts; throw an error at 1000
		int i = 0; // how many additional shifts we've removed
		while(i < adjustment)
		{
			for(int j = persons.size() - 1; j >= 0; j--)
			{
				Person p = persons.get(j);
				Shift s = p.getRemove();
				if(s == null)
					continue;
				p.unschedule(s);
				s.unschedule(p);
				i++;
			}
			if(errorCounter++ > 1000)
			{
				throw new RuntimeException("Can't remove enough shifts");
			}
		}
		
		for(Person p : persons)
		{
			System.out.println(p);
		}
		for(Shift s : shifts)
		{
			System.out.println(s);
		}
	}
}

class Shift
{
	private int day;
	private int position;
	private ArrayList<Person> assigned;
	private int capacity;
	
	public Shift(int d, int p, int c)
	{
		assigned = new ArrayList<Person>();
		day = d;
		position = p;
		capacity = c;
	}
	
	public void schedule(Person p)
	{
		assigned.add(p);
	}
	
	public void unschedule(Person p)
	{
		assigned.remove(p);
	}
	
	public int assignedCount()
	{
		return assigned.size();
	}
	
	public int getDay()
	{
		return day;
	}
	
	public int getPosition()
	{
		return position;
	}
	
	public int getCapacity()
	{
		return capacity;
	}
	
	public String toString()
	{
		String ret = "";
		ret += "This shift is #" + position + " on day #" + day + ".\n";
		ret += "The people who are scheduled for this shift are: ";
		for(Person p : assigned)
		{
			ret += p.getName() + " ";
		}
		return "\n" + ret;
	}
}

class Person implements Comparable<Person>
{
	private String name;
	private boolean designTeam;
	private ArrayList<Shift> scheduled;
	
	public Person(String name, boolean dt)
	{
		scheduled = new ArrayList<Shift>();
		this.name = name;
		designTeam = dt;
	}
	
	public int shiftCount()
	{
		return scheduled.size();
	}
	
	void schedule(Shift shift)
	{
		scheduled.add(shift);
	}
	
	void unschedule(Shift shift)
	{
		scheduled.remove(shift);
	}
	
	public Shift getRemove()
	{
		int best = -1;
		Shift bestShift = null;
		for(Shift s : scheduled)
		{
			if(!createsGap(s) && s.assignedCount() > s.getCapacity() && s.assignedCount() > best)
			{
				bestShift = s;
				best = s.assignedCount();
			}
		}
		if(bestShift != null)
			return bestShift;
		
		// try again, relaxing constraint on creating gap
		for(Shift s : scheduled)
		{
			if(s.assignedCount() > s.getCapacity() && s.assignedCount() > best)
			{
				bestShift = s;
				best = s.assignedCount();
			}
		}
		return bestShift;
	}
	
	boolean createsGap(Shift shift)
	{
		boolean leftEdge = false;
		boolean rightEdge = false;
		if(shift.getPosition() != 0 && isWorking(shift.getDay(), shift.getPosition() - 1))
			leftEdge = true;
		if(shift.getPosition() != ShiftScheduler.days.get(shift.getDay()) - 1 && isWorking(shift.getDay(), shift.getPosition() + 1))
			rightEdge = true;
		return leftEdge && rightEdge;
	}
	
	boolean isWorking(int day, int position)
	{
		for(Shift s : scheduled)
		{
			if(s.getDay() == day && s.getPosition() == position)
				return true;
		}
		return false;
	}
	
	public String getName()
	{
		return name;
	}
	
	public boolean isDesignTeam()
	{
		return designTeam;
	}
	
	public String toString()
	{
		String ret = "";
		ret += name + " is ";
		if(!designTeam)
			ret += "not ";
		ret += "on design team.\n";
		ret += "They are scheduled for " + scheduled.size() + " shifts.\n";
		return ret;
	}
	
	public int compareTo(Person b)
	{
		int compa = this.scheduled.size() + (designTeam ? ShiftScheduler.nonDesignTeamExtra : 0);
		int compb = b.scheduled.size() + (b.designTeam ? ShiftScheduler.nonDesignTeamExtra : 0);
		return compa - compb;
	}
}