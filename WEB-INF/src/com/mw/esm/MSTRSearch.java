package com.mw.esm;

import com.microstrategy.utils.MSTRCheckedException;
import com.microstrategy.utils.log.Level;
import com.microstrategy.web.app.tags.Log;
import com.microstrategy.web.objects.WebFolder;
import com.microstrategy.web.objects.WebObjectInfo;
import com.microstrategy.web.objects.WebObjectSource;
import com.microstrategy.web.objects.WebObjectsException;
import com.microstrategy.web.objects.WebSearch;

public class MSTRSearch {

  private WebSearch search;
  private String namePattern;
  private String abbreviationPattern;
  private boolean isAsync;
  private int objectType;
  private int domain;
  private WebObjectSource source;

  public MSTRSearch(WebObjectSource source, String namePattern, String abbreviationPattern, boolean isAsync, int objectType, int domain) {
    this.setSource(source);
    this.setNamePattern(namePattern);
    this.setAbbreviationPattern(abbreviationPattern);
    this.setAsync(isAsync);
    this.setObjectType(objectType);
    this.setDomain(domain);
    
    WebSearch search = source.getNewSearchObject();
    search.setNamePattern(this.getNamePattern());
    search.setAbbreviationPattern(this.getAbbreviationPattern());
    search.setAsync(this.isAsync());
    search.types().add(this.getObjectType());
    search.setDomain(this.getDomain());
    this.setSearch(search);
  }
  
  public WebObjectSource getSource() {
    return source;
  }

  public void setSource(WebObjectSource source) {
    this.source = source;
  }

  public String getNamePattern() {
    return namePattern;
  }

  public void setNamePattern(String namePattern) {
    this.namePattern = namePattern;
  }

  public String getAbbreviationPattern() {
    return abbreviationPattern;
  }

  public void setAbbreviationPattern(String abbreviationPattern) {
    this.abbreviationPattern = abbreviationPattern;
  }

  public boolean isAsync() {
    return isAsync;
  }

  public void setAsync(boolean isAsync) {
    this.isAsync = isAsync;
  }

  public int getObjectType() {
    return objectType;
  }

  public void setObjectType(int objectType) {
    this.objectType = objectType;
  }

  public int getDomain() {
    return domain;
  }

  public void setDomain(int domain) {
    this.domain = domain;
  }

  public MSTRSearch(WebSearch search) {
    this.setSearch(search);
  }

  public WebSearch getSearch() {
    return search;
  }

  public void setSearch(WebSearch search) {
    this.search = search;
  }

  public WebObjectInfo performSearch() throws MSTRCheckedException {
    WebObjectInfo result = null;
    try {
      this.search.submit();
      WebFolder folder = this.search.getResults();
      if (folder.size() > 0) {
        if (folder.size() == 1) {
          result = folder.get(0);
        } else {
          Log.logger.logp(Level.WARNING, "com.spscommerce.mstr.web.app.utils.MSTRSearch", "performSearch",
              "Search returned more than one result, returning first");
          result = folder.get(0);
        }
      }
    } catch (WebObjectsException ex) {
      throw new MSTRCheckedException(ex); //$NON-NLS-1$
    }
    return result;
  }

  public WebObjectInfo performSearch(boolean returnMultipleResults) throws MSTRCheckedException {

    WebObjectInfo result = null;

    if (!returnMultipleResults) {
      result = this.performSearch();
    } else {
      try {
        this.search.submit();
        WebFolder folder = this.search.getResults();
        if (folder.size() > 0) {
          result = folder;
        }
      } catch (WebObjectsException ex) {
        throw new MSTRCheckedException(ex); //$NON-NLS-1$
      }
    }
    
    return result;
  }
}
