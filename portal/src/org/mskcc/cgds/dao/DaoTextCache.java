package org.mskcc.cgds.dao;

import java.util.HashMap;

public class DaoTextCache
{
	private static HashMap<String, String> cache = new HashMap<String, String>();
	
	/**
	 * Generates an MD5 key for the given text.
	 * 
	 * @param text	text to hashed
	 * @return		an MD5 key corresponding to the given text
	 */
	public String generateKey(String text)
	{
		// TODO use MD5 hash
		String key = "H" + text.hashCode();
		
		return key;
	}
	
	public int cacheText(String key, String text)
	{
		// TODO check if it already exists, if not add text to DB (with a timestamp)
		cache.put(key, text);
		return 0;
	}
	
    public String getText(String key)
    {
    	// TODO get text from the cache (DB)
    	return cache.get(key);
    }
    
    public void deleteAllKeys()
    {
    	// TODO deleteAllKeys
    }
    
    public void purgeOldKeys()
    {
    	// TODO purgeOldKeys
    }
}
