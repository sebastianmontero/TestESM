package com.mw.esm;

import java.util.List;
import java.util.Locale;
import java.util.Locale.LanguageRange;

import com.microstrategy.utils.MSTRCheckedException;
import com.microstrategy.utils.MSTRUncheckedException;
import com.microstrategy.utils.StringUtils;
import com.microstrategy.utils.log.Level;
import com.microstrategy.utils.serialization.EnumWebPersistableState;
import com.microstrategy.web.app.AbstractExternalSecurity;
import com.microstrategy.web.app.EnumWebParameters;
import com.microstrategy.web.app.tags.Log;
import com.microstrategy.web.beans.RequestKeys;
import com.microstrategy.web.objects.EnumWebPreferences;
import com.microstrategy.web.objects.WebIServerSession;
import com.microstrategy.web.objects.WebObjectsException;
import com.microstrategy.web.objects.WebObjectsFactory;
import com.microstrategy.web.objects.WebSessionInfo;
import com.microstrategy.web.objects.WebSessionInfoList;
import com.microstrategy.web.objects.admin.users.WebUser;
import com.microstrategy.web.platform.ContainerServices;
import com.microstrategy.web.preferences.EnumPreferenceLevels;
import com.microstrategy.web.preferences.PreferenceLevel;
import com.microstrategy.web.preferences.Preferences;
import com.microstrategy.web.preferences.PreferencesMgr;
import com.microstrategy.webapi.EnumDSSXMLAuthModes;
import com.microstrategy.webapi.EnumDSSXMLObjectSubTypes;
import com.microstrategy.webapi.EnumDSSXMLSearchDomain;

public class TestESM extends AbstractExternalSecurity {
	
	private static final String SESSION_STATE = "SESSION_STATE";
	private static final String CLASS_NAME = "com.mw.esm.TestESM"; //$NON-NLS-1$

	public int handlesAuthenticationRequest(RequestKeys reqKeys, ContainerServices cntSvcs, int reason) {

		
		clearSessionState(cntSvcs);

		/*
		 * reason 1 = USE_MSTR_DEFAULT_LOGIN, NO_SESSION_FOUND,
		 * MISMATCHED_PREFERENCES, AUTHENTICATION_REQUEST
		 * 
		 * reason 4 = LOGIN_FIRST
		 * 
		 * reason 2 = Session expired
		 */
		Log.logger.log(Level.INFO, "Reason: " + reason);// Authentication
		// Request
		if (reason == 1 || reason == 4 || reason == 2) {

			try {


				// If exists, let in
				boolean success = false;
				try {
					success = obtainWebIServerSession(getLocale(cntSvcs),reqKeys, cntSvcs, "MWT7", "MWTech2016!");
				} catch(MSTRCheckedException e){
					success = obtainWebIServerSession(getLocale(cntSvcs),reqKeys, cntSvcs, "WIN-5E4UBCM2826", "");
				}

				if (success) {
					return COLLECT_SESSION_NOW;
				} else {
					return USE_CUSTOM_LOGIN_PAGE;
				}
			} catch (Exception e) {
				Log.logger.log(Level.INFO, "Exception: " + e);
			}

			return USE_CUSTOM_LOGIN_PAGE;

		}
		return USE_CUSTOM_LOGIN_PAGE;
	}
	
	
	private Locale getLocale(ContainerServices cntSvcs){
		String acceptLanguage = cntSvcs.getHeaderValue("accept-language");
		if(acceptLanguage == null || "".equals(acceptLanguage)){
			return null;
		}
		List<LanguageRange> languageRanges = Locale.LanguageRange.parse(acceptLanguage);
		if(languageRanges.size() == 0){
			return null;
		}
		return Locale.forLanguageTag(languageRanges.get(0).getRange());
	}

	private void clearSessionState(ContainerServices cntSvcs) {
		String sessionState = (String) cntSvcs.getSessionAttribute(SESSION_STATE);

		if (!StringUtils.isEmpty(sessionState)) {
			closeISSession(sessionState);
			cntSvcs.setSessionAttribute(SESSION_STATE, null);
		}
	}

	public WebIServerSession getWebIServerSession(RequestKeys reqKeys, ContainerServices cntSvcs) {

		String newProject = reqKeys.getValue(EnumWebParameters.WebParameterLoginProjectName);


		String METHOD_NAME = "getWebIServerSession()";
		WebIServerSession userSession = null;
		try {
			String sessionState = (String) cntSvcs.getSessionAttribute(SESSION_STATE);

			if (StringUtils.isEmpty(sessionState)) {
				Log.logger.logp(Level.SEVERE, CLASS_NAME, METHOD_NAME, "Session State was empty");
				throw new MSTRUncheckedException("Session state was empty!");
			}

			userSession = WebObjectsFactory.getInstance().getIServerSession();
			userSession.restoreState(sessionState);

			if (newProject != null && StringUtils.isNotEqual(newProject, userSession.getProjectName())) {
				userSession.setProjectName(newProject);
			}
			if (!userSession.isAlive()) {
				userSession.reconnect();
				String newState = userSession.saveState(EnumWebPersistableState.MAXIMAL_STATE_INFO);
				cntSvcs.setSessionAttribute(SESSION_STATE, newState);
			}
			
		} catch (WebObjectsException e) {
			cntSvcs.setSessionAttribute(SESSION_STATE, null);

			throw new MSTRUncheckedException("WebIServerSession.getWebIServerSession(): Unable to restore session");
		}
		return userSession;
	}

	public boolean isRequestAuthorized(RequestKeys reqKeys, ContainerServices cntSvcs, WebIServerSession user) {

		// filter

		String METHOD_NAME = "isRequestAuthorized(RequestKeys, ContainerServices, WebIServerSession)"; //$NON-NLS-1$

		Log.logger.logp(Level.INFO, CLASS_NAME, METHOD_NAME,
				String.format("Checking if request is authorized reqKeys: %1$s, user: %2$s",
						DebugUtils.getRequestKeysAsString(reqKeys), user));// Log
																			// creating
																			// security

		WebSessionInfoList sessionList = user.getSessionList();
		WebSessionInfo winfo = null;
		String login = null;
		try {
			if ((user.isAlive()) && (user.isActive())) {
				Log.logger.logp(Level.INFO, CLASS_NAME, METHOD_NAME, "User is alive and active");// Log
																									// creating
																									// security
				for (int y = 0; y < sessionList.size(); y++) {
					winfo = sessionList.get(y);
					Log.logger.logp(Level.INFO, CLASS_NAME, METHOD_NAME,
							String.format("SessionInfo: %1$s, pos: %2$s", winfo, y));// Log
																						// creating
																						// security
					if (!winfo.isActive()) {
						Log.logger.logp(Level.INFO, CLASS_NAME, METHOD_NAME,
								String.format("SessionInfo not active: %1$s, pos: %2$s", winfo, y));// Log
																									// creating
																									// security
						WebIServerSession session = (WebIServerSession) winfo;
						login = winfo.getUserInfo().getAbbreviation();
						/*
						 * if (session.getProjectName().length() != 0) {
						 * sessionList.remove(winfo, true); }
						 */
					}
				}
			}
		} catch (WebObjectsException e) {
			Log.logger.logp(Level.WARNING, CLASS_NAME, METHOD_NAME,
					"Exception thrown while removing session from session list for user " + login, e); //$NON-NLS-1$
		}

		return super.isRequestAuthorized(reqKeys, cntSvcs, user);

	}

	public boolean obtainWebIServerSession(Locale locale, RequestKeys reqKeys, ContainerServices cntSvcs, String iServer, String password) throws MSTRCheckedException {

		String METHOD_NAME = "obtainWebIServerSession()"; //$NON-NLS-1$

		Log.logger.logp(Level.INFO, CLASS_NAME, METHOD_NAME, "Obtaining Web IServer Session");// Log
																								// creating

		String sessionState = null;

		
		WebIServerSession iss = WebObjectsFactory.getInstance().getIServerSession();
		iss.setServerName(iServer);
		String project = reqKeys.getValue(EnumWebParameters.WebParameterLoginProjectName);
		String user = reqKeys.getValue("user");
		//iss.setLogin("Administrator");
		//iss.setPassword(password);
		String pass;
		if(user == null || user.equals("")){
			user = "Administrator";
			pass = password;
		} else {
			pass = user;
		}
		iss.setLogin(user);
		iss.setPassword(pass);
		iss.setServerPort(0);
		iss.setProjectName(project);
		iss.setAuthMode(EnumDSSXMLAuthModes.DssXmlAuthStandard);
		//iss.setApplicationType(EnumDSSXMLAuthModes.DssXmlAuthStandard);
		setSessionLocale(iss, locale);
		iss.getSessionID();
		
		/*MSTRSearch userSearch = new MSTRSearch(iss.getFactory().getObjectSource(), null,
				"Administrator", false, EnumDSSXMLObjectSubTypes.DssXmlSubTypeUser,
				EnumDSSXMLSearchDomain.DssXmlSearchDomainRepository);

		WebUser webUser = (WebUser) userSearch.performSearch();*/

		sessionState = iss.saveState(EnumWebPersistableState.MAXIMAL_STATE_INFO);
		cntSvcs.setSessionAttribute(SESSION_STATE, sessionState);
		if(project != null){
			String documentId = reqKeys.getValue("did");
			String url;
			if(documentId == null || documentId.equals("")){
				url = null;
			} else {
				url = "http://localhost:8080/MicroStrategy/servlet/mstrWeb?Project=Geodash&Port=0&evt=2048001&src=mstrWeb.2048001&currentViewMedia=1&visMode=0&documentID=" +  documentId;
			}
			Preferences prefs = PreferencesMgr.getInstance().getUserPreferences(iss);
			prefs.setValue(EnumWebPreferences.WebPreferenceStartPage, EnumPreferenceLevels.USER_PROJECT, url);
			prefs.save();
		}
		return true;

		
	}
	
	private void setSessionLocale(WebIServerSession iss, Locale locale){
		
		if(locale != null){
			iss.setLocale(locale);
			iss.setDisplayLocale(locale);
			iss.setMessagesLocale(locale);
			iss.setMetadataLocale(locale);
			iss.setNumberLocale(locale);
			iss.setWarehouseDataLocale(locale);
		}
	}
	
	

	public String getCustomLoginURL(String originalURL, String desiredIServer, int desiredPort, String desiredProject) {

		return "";

	}

	public String getFailureURL(int reqType, ContainerServices cntrSvcs) {
		return "";
	}

	private boolean closeISSession(String sessionState) {
		try {
			WebIServerSession iss = WebObjectsFactory.getInstance().getIServerSession();

			iss.restoreState(sessionState);
			iss.closeSession();
			return true;
		} catch (WebObjectsException e) {
			// most likely the ISeverSession has already expired or closed.
			return false;
		}
	}

	public boolean reconnectISSession(String sessionState, ContainerServices cntSvcs, RequestKeys reqKeys) {

		// reconnect session
		WebIServerSession iss = WebObjectsFactory.getInstance().getIServerSession();
		iss.restoreState(sessionState);

		String newProject = reqKeys.getValue(EnumWebParameters.WebParameterLoginProjectName);
		if (newProject != null && StringUtils.isNotEqual(newProject, iss.getProjectName())) {

			// Note: This sample code doesn't support single sign-on across
			// Intelligence Servers
			iss.setProjectName(newProject);
		}

		try {

			if (!iss.isAlive()) {
				iss.reconnect();
				String newState = iss.saveState(EnumWebPersistableState.MAXIMAL_STATE_INFO);
				cntSvcs.setSessionAttribute(SESSION_STATE, newState);
			}
		} catch (WebObjectsException e) {
			// if cannot reconnect, most likely the credential has been changed.
			Log.logger.log(Level.INFO, "Session state cannot be reused");
			return false;
		}
		return true;
	}

}
