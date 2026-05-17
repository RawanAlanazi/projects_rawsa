package net.sf.jcablib;
/*
* CabLib, a library for extracting MS cabinets
* Copyright (C) 1999, 2002  David V. Himelright
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Library General Public
* License as published by the Free Software Foundation; either
* version 2 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Library General Public License for more details.
*
* You should have received a copy of the GNU Library General Public
* License along with this library; if not, write to the
* Free Software Foundation, Inc., 59 Temple Place - Suite 330,
* Boston, MA  02111-1307, USA.
*
* David Himelright can be reached at:
* <david@litfit.org> 
*/
import java.io.*;
import java.util.*;
import java.util.zip.*;
/**
* This class presents cabinet files via the familar java.util.zip interface.
* @author David Himelright <a href="mailto:david@litfit.org">david@litfit.org</a>
*/
public
class CabFile
implements CabConstants {
	
	// Globals --------------------
	public
	CabEntry[]			entries;
	CabFolder[]			folders;
	File				mFile;
	
	// Cab data -------------------
	public
	short	cab_version,
			cab_folders,
			cab_files,
			cab_flags,
			cab_setid,				//cab set id
			cab_icab;				//zero-based cabinet number (huh???)
	public
	int		cab_signature,			//file signature 'MSCF'
			cab_header_checksum,	
			cab_file_size,
			cab_folder_checksum,
			cab_entry_offset,		//offset of first cab entry
			cab_file_checksum,
			file_size;
	public 
	boolean	hasprev = false,
			hasnext = false;
	String	prevname,
			nextname,
			prevnum,
			nextnum;
	
	/**
	* @param inFile A reference to a Cabinet file.
	* @exception CabException When the file header is malformed.
	* @exception IOException thrown by the IO classes.
	*/
	public CabFile(File inFile)
	throws IOException {
		this.mFile = inFile;
		readHeader();
	}
	
	/**
	* @param name A path to a Cabinet file.
	* @exception CabException When the file header is malformed.
	* @exception IOException thrown by the IO classes.
	*/
	public CabFile(String name)
	throws IOException {
		this(new File(name));
	}
	
	/**
	* @exception CabException When the file header is malformed.
	* @exception IOException thrown by the IO classes.
	*/
	private synchronized void readHeader()
	throws IOException {
		RandomAccessFile raf = new RandomAccessFile(mFile, "r");
		CabFolder crapFolder = new CabFolder(null, 0, (short)0, kInvalidFolder, -1);
		
		file_size = (int)raf.length();
		
		//read HEADER
		cab_signature		= raf.readInt();
		cab_header_checksum	= raf.readInt();
		cab_file_size		= rotateInt(raf.readInt());
		cab_folder_checksum	= raf.readInt();
		cab_entry_offset	= rotateInt(raf.readInt());
		cab_file_checksum	= raf.readInt();
		cab_version			= rotateShort(raf.readShort());
		cab_folders			= rotateShort(raf.readShort());
		cab_files			= rotateShort(raf.readShort());
		cab_flags			= rotateShort(raf.readShort());
		cab_setid			= rotateShort(raf.readShort());
		cab_icab			= rotateShort(raf.readShort());
		
		//detect errors
		if(cab_signature != MSCF)
			throw new CabException(mFile.getName() + " has an invalid signature.");
		if(cab_files < 1 || cab_folders < 1)
			throw new CabException(mFile.getName() + " has less than zero files (invalid header).");
		
		//read series data (if a cab is a part of an installer or series)
		if((kFlagHasPrev & cab_flags) == kFlagHasPrev) {
			this.hasprev = true;
			prevname = readCString(raf);
			prevnum = readCString(raf);
		}
		if((kFlagHasNext & cab_flags) == kFlagHasNext) {
			this.hasnext = true;
			nextname = readCString(raf);
			nextnum = readCString(raf);
		}
		if((kFlagReserve & cab_flags) == kFlagReserve)
			raf.seek(cab_entry_offset - (8 * cab_folders));
		
		//read cabfolders (compressed chunks)
		folders = new CabFolder[cab_folders];
		for(int i=0; i<cab_folders; i++) {
			folders[i] = new CabFolder(this.mFile,						//self reference
										rotateInt(raf.readInt()),		//folder offset
										rotateShort(raf.readShort()),	//folder data
										rotateShort(raf.readShort()),	//folder compression method
										i);								//folder number
		}
		
		//read entries
		entries = new CabEntry[cab_files];
		raf.seek(cab_entry_offset);
		
		
		int inflated_size;
		int inflated_offset;
		short folder_ix;
		int timestamp;
//		short date;
//		short time;
		short attribs;		
		String name;
		CabFolder cabfolder;
		
		
		for(int i=0; i<entries.length; i++) {
			
			inflated_size = rotateInt(raf.readInt());
			inflated_offset = rotateInt(raf.readInt());
			folder_ix = rotateShort(raf.readShort());
			timestamp = rotateInt(raf.readInt());
//			mDate = raf.readShort();
//			mTime = raf.readShort();
			attribs = raf.readShort();		
			name = readCString(raf);
			//extrapolated information
			
			if(folder_ix >= 0)
				cabfolder = folders[folder_ix];
			else
				cabfolder = crapFolder;
			
			entries[i] = new CabEntry(name, inflated_size, inflated_offset, timestamp,
										folder_ix, attribs, cabfolder);
		}
		raf.close();
	}
	
	/**
	* @return the name of the cabinet file
	*/
	public String getName() {
		return mFile.getName();
	}
	
	/**
	* @return the file
	*/
	public File getFile() {
		return mFile;
	}
	
	
	/**
	* Rotate an int's byte order; switch big to little or little to big endian
	* @param i the integer you'd like to reverse
	* @return an integer with it's byte order reversed
	*/
	public static int rotateInt(int i) {
		int[] bytes = new int[4];
		bytes[0] = (i >> 24) & 0x000000FF;
		bytes[1] = (i >> 8) & 0x0000FF00;
		bytes[2] = (i << 8) & 0x00FF0000;
		bytes[3] = (i << 24) & 0xFF000000;
		return bytes[0] + bytes[1] + bytes[2] + bytes[3];
	}
	
	/**
	* Rotate an short's byte order; switch big to little or little to big endian
	* @param s the short you'd like to reverse
	* @return an short with it's byte order reversed
	*/
	public static short rotateShort(short s) {
		short[] bytes = new short[2];
		bytes[0] = (short)(s >> 8);
		bytes[1] = (short)(s << 8);
		return (short)(bytes[0] + bytes[1]);
	}
	
	/**
	* Reads bytes til an empty byte is encountered, then returns them as a string.
	* XXXMaximum length is 256 characters, shouldn't be a problem for now, that's the
	* maximum length of a DOS path as I understand it.
	* @param in the DataInput to read your string from
	* @return The chunk of data before the next empty byte as a String.
	* @exception IOException Thrown by DataInput.
	*/
	public static String readCString(DataInput in)
	throws IOException {
		int		i = 0;
		byte[]	temp = new byte[256];
		byte	nil = 0x00;
		
		while(true) {
			temp[i] = in.readByte();
			if(temp[i] == nil || i>255)
				break;
			i++;
		}
		return new String(temp).substring(0, i);
	}
	
	/**
	* Included for parity with java.util.zip.ZipFile interface.
	* @return an enumeration containing references to this file's CabEntries
	*/
	public Enumeration entries() {
		Vector v = new Vector(entries.length);
		v.copyInto(entries);
		return v.elements();
	}

	/** 
	* @return all CabEntries in this file as an array
	*/
	public CabEntry[] getEntries() {
		return entries;
	}

	/**
	* unique to CabFile
	* @return an enumeration containing references to this file's CabFolders
	*/
	public Enumeration folders() {
		Vector v = new Vector(folders.length);
		v.copyInto(folders);
		return v.elements();
	}

	/** 
	* @return all CabFolders in this file as an array
	*/
	public CabFolder[] getFolders() {
		return folders;
	}

	/**
	* Included for parity with java.util.zip.ZipFile interface.
	* @param name the name of the CabEntry to match
	* @return a CabEntry whose name matches the string provided
	*/
	public CabEntry getEntry(String name) {
		int i = 0;
		while(true) {
			if(entries[i].getName().equals(name))
				return entries[i];
			i++;
		}
	}
	
	/**
	* Included for parity with java.util.zip.ZipFile interface.
	* This method does nothing at all, the RandomAccessFile is opened and closed in 
	* the readHeader method.
	*/
	public void close() {}
	
	/**
	* Create a new CabFileInputStream for reading the current entry.  This is the
	* slow way of doing things, but it was added to provide an interface identical
	* to java.util.zip.ZipFile
	* @param ce retrieve an InputStream to this entry
	* @return an InputStream that returns uncompressed data
	* @exception CabException thrown by CabFileInputStream constructor
	* @exception IOException thrown by CabFileInputStream constructor
	*/
	public InputStream getInputStream(CabEntry ce)
	throws IOException {
		return new CabFileInputStream(ce);
	}
	
	/**
	* Create a new CabFileInputStream for reading the current entry.  This is the
	* slow way of doing things, but it was added to provide an interface identical
	* to java.util.zip.ZipFile
	* @param cf retrieve an InputStream to this folder
	* @return an InputStream that returns uncompressed data
	* @exception CabException thrown by CabFileInputStream constructor
	* @exception IOException thrown by CabFileInputStream constructor
	*/
	public InputStream getInputStream(CabFolder cf)
	throws IOException {
		return new CabFileInputStream(cf);
	}
}
