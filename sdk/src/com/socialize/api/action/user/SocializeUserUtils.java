/*
 * Copyright (c) 2012 Socialize Inc.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.socialize.api.action.user;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import com.socialize.Socialize;
import com.socialize.SocializeService;
import com.socialize.api.SocializeSession;
import com.socialize.api.action.SocializeActionUtilsBase;
import com.socialize.auth.AuthProviderType;
import com.socialize.entity.SocializeAction;
import com.socialize.entity.User;
import com.socialize.error.SocializeException;
import com.socialize.listener.user.UserGetListener;
import com.socialize.listener.user.UserSaveListener;
import com.socialize.log.SocializeLogger;
import com.socialize.networks.SocialNetwork;
import com.socialize.ui.action.ActionDetailActivity;
import com.socialize.ui.comment.CommentDetailActivity;
import com.socialize.ui.profile.ProfileActivity;
import com.socialize.ui.profile.UserSettings;


/**
 * @author Jason Polites
 *
 */
public class SocializeUserUtils extends SocializeActionUtilsBase implements UserUtilsProxy {

	private UserSystem userSystem;
	private SocializeLogger logger;

	@Override
	public UserSettings getUserSettings(Context context) {
		return getSocialize().getSession().getUserSettings();
	}
	
	@Override
	public void showUserSettingsViewForResult(Activity context, Long userId, int requestCode) {
		Intent i = newIntent(context, ProfileActivity.class);
		i.putExtra(Socialize.USER_ID, userId.toString());
		
		try {
			context.startActivityForResult(i, requestCode);
		} 
		catch (ActivityNotFoundException e) {
			logger.error("Could not find ProfileActivity.  Make sure you have added this to your AndroidManifest.xml");
		}	
	}
	
	@Override
	public void showUserSettingsView(Activity context, Long userId) {
		Intent i = newIntent(context, ProfileActivity.class);
		i.putExtra(Socialize.USER_ID, userId.toString());
		try {
			context.startActivity(i);
		} 
		catch (ActivityNotFoundException e) {
			logger.error("Could not find ProfileActivity.  Make sure you have added this to your AndroidManifest.xml");
		}
	}
	
	@Override
	public void showUserProfileView(Activity context, User user, SocializeAction action) {
		Intent i = newIntent(context, ActionDetailActivity.class);
		i.putExtra(Socialize.USER_ID, user.getId().toString());
		
		if(action != null) {
			i.putExtra(Socialize.ACTION_ID, action.getId().toString());
		}
		
		try {
			// MUST be FLAG_ACTIVITY_SINGLE_TOP because we only code to onNewIntent
			i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			
			context.startActivity(i);
		} 
		catch (ActivityNotFoundException e) {
			// Revert to legacy
			i.setClass(context, CommentDetailActivity.class);
			try {
				context.startActivity(i);
				if(logger != null) logger.warn("Using legacy CommentDetailActivity.  Please update your AndroidManifest.xml to use ActionDetailActivity");
			} 
			catch (ActivityNotFoundException e2) {
				if(logger != null) logger.error("Could not find ActionDetailActivity.  Make sure you have added this to your AndroidManifest.xml");
			}
		}		
	}

	@Override
	public SocialNetwork[] getAutoPostSocialNetworks(Context context) {
		SocializeService socialize = Socialize.getSocialize();
		UserSettings user = getUserSettings(context);
		
		SocialNetwork[] networks = null;
		
		if(user.isAutoPostFacebook() && socialize.isAuthenticated(AuthProviderType.FACEBOOK)) {
			if(user.isAutoPostTwitter() && socialize.isAuthenticated(AuthProviderType.TWITTER)) {
				networks = new SocialNetwork[]{SocialNetwork.FACEBOOK, SocialNetwork.TWITTER};
			}
			else {
				networks = new SocialNetwork[]{SocialNetwork.FACEBOOK};
			}
		}
		else if(user.isAutoPostTwitter() && socialize.isAuthenticated(AuthProviderType.TWITTER)) {
			networks = new SocialNetwork[]{SocialNetwork.TWITTER};
		}
		return networks;
	}

	@Override
	public User getCurrentUser(Context context) throws SocializeException  {
		return getCurrentUser(context, false);
	}
	
	protected User getCurrentUser(Context context, boolean failOnError) throws SocializeException  {
		SocializeSession session = getSocialize().getSession();
		
		if(session != null) {
			User user = session.getUser();
			if(user != null) {
				return user;
			}
		}
		
		// We couldn't get a user, this shouldn't happen
		if(failOnError) {
			throw new SocializeException("No user returned from getCurrentUser after second attempt");
		}
		else {
			if(!Socialize.getSocialize().isInitialized(context)) {
				Socialize.getSocialize().init(context);
			}
			Socialize.getSocialize().authenticateSynchronous(context);
			return getCurrentUser(context, true);
		}
	}
	
	@Override
	public void getUser(Context context, long id, UserGetListener listener) {
		userSystem.getUser(Socialize.getSocialize().getSession(), id, listener);
	}

	@Override
	public void saveUserSettings(Context context, UserSettings userSettings, UserSaveListener listener) {
		userSystem.saveUserSettings(context, getSocialize().getSession(), userSettings, listener);
	}

	public void setUserSystem(UserSystem userSystem) {
		this.userSystem = userSystem;
	}
	
	public void setLogger(SocializeLogger logger) {
		this.logger = logger;
	}

	@Override
	public void clearSession(Context context) {
		getSocialize().clearSessionCache(context);
	}
	
	protected Intent newIntent(Activity context, Class<?> cls) {
		return new Intent(context, cls);
	}
}
