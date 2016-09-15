/*
 * Double Auction for Relay Assignment
 * Copyright (C) 2011 Zichuan Xu
 *
 */

package utils;

/**
 * Utility class for handing out unique ids.
 * 
 * A using class wishing to assign unique ids to each of its instances should
 * declare a static member variable:
 * 
 * <code>
 *   static IdAllocator idAllocator = new IdAllocator();
 * </code>
 * 
 * In its constructor it should use something like:
 * 
 * <code>
 *   id = idAllocator.nextId();
 * </code>
 */

public class IdAllocator {

	protected long nextId = 5000;

	public IdAllocator() {
	}

	public synchronized long nextId() {
		return nextId++;
	}

}