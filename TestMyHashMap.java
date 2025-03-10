/*
Bradley Stone
COSC 2336
Programming Assignment 10
Due: 7/26/2023
Submitted: 7/26/2023

In this program we use the given MyHashMap class and modify it so that it now uses linear collision detection as opposed to the bucket method.
We then test if the HashMap is created and collisions correctly with a test class below the code. 
*/

import java.util.*;
public class TestMyHashMap<K,V> implements MyMap<K, V> {
	// Define the default hash-table size. Must be a power of 2
	private static int DEFAULT_INITIAL_CAPACITY = 4;

	// Define the maximum hash-table size. 1 << 30 is same as 2^30
	private static int  MAXIMUM_CAPACITY = 1 << 30;

	// Current hash-table capacity. Capacity is a power of 2
	private int capacity;

	// Define default load factor
	private static float DEFAULT_MAX_LOAD_FACTOR = 0.5f;

	// Specify a load factor used in the hash table
	private float loadFactorThreshold;

	// The number of entries in the map 
	private int size = 0;

	// Hash table is an ArrayList
	ArrayList<MyMap.Entry<K, V>> table;

	/** Construct a map with the default capacity and load factor */
	public TestMyHashMap() {
		this(DEFAULT_INITIAL_CAPACITY, DEFAULT_MAX_LOAD_FACTOR);
	}

	/** Construct a map with the specified initial capacity and 
	 * default load factor */
	public TestMyHashMap(int initialCapacity) {
		this(initialCapacity, DEFAULT_MAX_LOAD_FACTOR);
	}

	/** Construct a map with the specified initial capacity
	 * and load factor */
	public TestMyHashMap(int initialCapacity, float loadFactorThreshold) {
		if (initialCapacity > MAXIMUM_CAPACITY)
			this.capacity = MAXIMUM_CAPACITY;
		else
			this.capacity = trimToPowerOf2(initialCapacity);

		this.loadFactorThreshold = loadFactorThreshold;
		table = new ArrayList<>();
		for (int i = 0; i < capacity; i++)
			table.add(null);
	}

	@Override /** Remove all of the entries from this map */
	public void clear() {
		size = 0;
		removeEntries();
	}

	@Override /** Return true if the specified key is in the map */
	public boolean containsKey(K key) {
		if (get(key) != null)
			return true;
		else
			return false;
	}

	 @Override /** Return true if this map contains the value */ 
	  public boolean containsValue(V value) {
	    for (int i = 0; i < capacity; i++) {
	      if (table.get(i) != null) {
	          if (table.get(i).equals(value)) 
	            return true;
	      }
	    }
	    
	    return false;
	  }

	  @Override /** Return a set of entries in the map */
	  public java.util.Set<MyMap.Entry<K,V>> entrySet() {
	    java.util.Set<MyMap.Entry<K, V>> set = 
	      new java.util.HashSet<>();
	    
	    for (int i = 0; i < capacity; i++) {
	      if (table.get(i)!= null) {
	          set.add(table.get(i)); 
	      }
	    }
	    
	    return set;
	  }

	  @Override /** Return the value that matches the specified key */
		public V get(K key) {
			int index = hash(key.hashCode());
			if(table.get(index) == null) {
				table.add(index, null);
			}
			while(table.get(index) != null) {
				if (table.get(index).getKey().equals(key)) {
						return table.get(index).getValue();
				}
				index++;
				index = index % capacity;
			}

			return null;
		}

	@Override /** Return true if this map contains no entries */
	public boolean isEmpty() {
		return size == 0;
	}

	 @Override /** Return a set consisting of the keys in this map */
		public java.util.Set<K> keySet() {
			java.util.Set<K> set = new java.util.HashSet<K>();

			for (int i = 0; i < capacity; i++) {
				if (table.get(i) != null)
					set.add(table.get(i).getKey());
			}

			return set;
		}

	@Override /** Add an entry (key, value) into the map */
	public V put(K key, V value) {
		int index = hash(key.hashCode());
		if (table.get(index)==null)
			table.add(null);
		if (table.get(index) != null) {
			// The key is already in the map
			if (table.get(index).getKey().equals(key)) {
				Entry<K, V> insert = table.get(index);
				V prevalue = insert.getValue();
				// Replace old value with new value
				insert.value = value;
				table.set(index, insert);
				// Return the old value for the key
				return prevalue;
			}

			// Collision check if the next index is available
			index++; 
			index = index % capacity;
		}

		// Check load factor
		if (size >= capacity * loadFactorThreshold) {
			if (capacity == MAXIMUM_CAPACITY)
				throw new RuntimeException("Exceeding maximum capacity");
			rehash();
		}

		// Add a new entry (key, value) to hashtable
		table.set(index, new MyMap.Entry<K, V>(key, value));

		size++; // Increase size

		return value;
	}  

	@Override /** Remove the entry for the specified key */
	public void remove(K key) {
		int index = hash(key.hashCode());
		
		// Remove the entry that matches the key
		if (key != null) {
			if (table.get(index).getKey().equals(key)) {
				table.remove(index);
				size--; // Decrease size
			}
			index++;
			index = index % capacity;
		}
	}


	@Override /** Return the number of entries in this map */
	public int size() {
		return size;
	}

	@Override /** Return a set consisting of the values in this map */
	  public java.util.Set<V> values() {
	    java.util.Set<V> set = new java.util.HashSet<>();
	    
	    for (int i = 0; i < capacity; i++) {
	      if (table.get(i) != null) {
	          set.add(table.get(i).getValue()); 
	      }
	    }
	    
	    return set;
	  }

	/** Hash function */
	private int hash(int hashCode) {
		return supplementalHash(hashCode) & (capacity - 1);
	}

	/** Ensure the hashing is evenly distributed */
	private static int supplementalHash(int h) {
		h ^= (h >>> 20) ^ (h >>> 12);
		return h ^ (h >>> 7) ^ (h >>> 4);
	}

	/** Return a power of 2 for initialCapacity */
	private int trimToPowerOf2(int initialCapacity) {
		int capacity = 1;
		while (capacity < initialCapacity) {
			capacity <<= 1;
		}

		return capacity;
	}

	 /** Remove all entries from each bucket */
	  private void removeEntries() {
	    table.clear();
	  }

	  /** Rehash the map */
	  private void rehash() {
			java.util.Set<Entry<K, V>> set = entrySet();
			capacity <<= 1; // Same as capacity *= 2. <= is more efficient
			table.clear(); // Clear the hash table
			size = 0; // Reset size to 0
			for (int i = 0; i < capacity; i++)
				table.add(null);

			for (Entry<K, V> entry : set) {
				put(entry.getKey(), entry.getValue());
			}
		}


	@Override /** Return a string repesentation for this map */
	public String toString() {
		StringBuilder builder = new StringBuilder("[");

		for (Entry<K, V> entry: table) {
			if (entry != null && table.size() > 0)
				builder.append(entry);
		}

		builder.append("]");
		return builder.toString();
	}


public static void main(String[] args) {
		    // Create a map
		    MyMap<String, Integer> map = new TestMyHashMap<>();
		  
		    //places data into hashmap//
		   
		    map.put("Lewis", 29);
		    map.put("Smith", 65);
		    map.put("Anderson", 31);
		    map.put("Cook", 29);// Add Smith with age 30 to map
		    

		    //prints entries in map//
		    System.out.println("Entries in map: " + map);
		    //prints age for Lewis//
		    System.out.println("The age for " + "Lewis is " +
		      map.get("Lewis"));
		    //checks if Smith is in map//
		    System.out.println("Is Smith in the map? " + 
		      map.containsKey("Smith"));
		  //checks if age 33 is in map//
		    System.out.println("Is age 33 in the map? " + 
		      map.containsValue(33));
		 // Removes Smith from map//
		    map.remove("Smith"); 
		    //prints map after removal//
		    System.out.println("Entries in map after removing Smith: " + map);
		    //clears all of map//
		    map.clear();
		    System.out.println("Entries after clearing map: " + map);
		  }
		}
