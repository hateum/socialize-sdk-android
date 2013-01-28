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
package com.socialize.networks.facebook;

import java.io.IOException;
import java.util.Map;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import com.socialize.ConfigUtils;
import com.socialize.Socialize;
import com.socialize.SocializeService;
import com.socialize.api.action.share.SocialNetworkShareListener;
import com.socialize.auth.AuthProviderType;
import com.socialize.auth.EmptyAuthProvider;
import com.socialize.config.SocializeConfig;
import com.socialize.entity.Entity;
import com.socialize.facebook.Facebook;
import com.socialize.listener.SocializeAuthListener;
import com.socialize.networks.SocialNetworkPostListener;
import com.socialize.networks.SocializeDeAuthListener;
import com.socialize.util.ImageUtils;


/**
 * @author Jason Polites
 *
 */
@SuppressWarnings("deprecation")
public class FacebookUtilsImpl implements FacebookUtilsProxy {
	
	private FacebookFacade facebookFacade;
	private ImageUtils imageUtils;
	private Facebook facebook;
	private SocializeConfig config;

	/**
	 * DO NOT CALL
	 */
	@Deprecated
	@Override
	public synchronized Facebook getFacebook(Context context) {
		if(facebook == null) {
			facebook = new Facebook(config.getProperty(SocializeConfig.FACEBOOK_APP_ID));
		}
		return facebook;
	}

	/* (non-Javadoc)
	 * @see com.socialize.networks.facebook.FacebookUtilsProxy#link(android.app.Activity, com.socialize.listener.SocializeAuthListener)
	 */
	@Override
	public void link(Activity context, SocializeAuthListener listener) {
		facebookFacade.link(context, listener);
	}
	
	@Override
	public void link(Activity context, SocializeAuthListener listener, String... permissions) {
		facebookFacade.link(context, listener, permissions);
	}

	/* (non-Javadoc)
	 * @see com.socialize.networks.facebook.FacebookUtilsProxy#link(android.app.Activity, java.lang.String, com.socialize.listener.SocializeAuthListener)
	 */
	@Override
	public void link(final Activity context, final String token, final boolean verifyPermissions, final SocializeAuthListener listener) {
		facebookFacade.link(context, token, verifyPermissions, listener);
	}

	/* (non-Javadoc)
	 * @see com.socialize.networks.facebook.FacebookUtilsProxy#unlink(android.app.Activity)
	 */
	@Override
	public void unlink(final Context context, final SocializeDeAuthListener listener) {
		facebookFacade.unlink(context, listener);
	}

	/* (non-Javadoc)
	 * @see com.socialize.networks.facebook.FacebookUtilsProxy#isLinked(android.content.Context)
	 */
	@Override
	public boolean isLinked(Context context) {
		return facebookFacade.isLinked(context);
	}

	/* (non-Javadoc)
	 * @see com.socialize.networks.facebook.FacebookUtilsProxy#isAvailable(android.content.Context)
	 */
	@Override
	public boolean isAvailable(Context context) {
		return getSocialize().isSupported(AuthProviderType.FACEBOOK);
	}

	/* (non-Javadoc)
	 * @see com.socialize.networks.facebook.FacebookUtilsProxy#setAppId(android.content.Context, java.lang.String)
	 */
	@Override
	public void setAppId(Context context, String appId) {
		SocializeConfig config = ConfigUtils.getConfig(context);
		config.setFacebookAppId(appId);
	}

	/* (non-Javadoc)
	 * @see com.socialize.networks.facebook.FacebookUtilsProxy#getAccessToken(android.content.Context)
	 */
	@Override
	public String getAccessToken(Context context) {
		return facebookFacade.getAccessToken(context);
	}

	@Override
	public void postEntity(final Activity context, final Entity entity, final String text, final SocialNetworkShareListener listener) {
		facebookFacade.postEntity(context, entity, text, listener);		
	}
	
	@Override
	public void extendAccessToken(final Activity context, final SocializeAuthListener listener) {
		facebookFacade.extendAccessToken(context, listener);
	}
	
	@Override
	public void getCurrentPermissions(Activity context, String token, OnPermissionResult callback) {
		facebookFacade.getCurrentPermissions(context, token, callback);
	}

	@Override
	public void getCurrentPermissions(Activity context, String token, FacebookPermissionCallback callback) {
		throw new UnsupportedOperationException("This method is no longer supported");
	}

	@Override
	public void post(Activity context, String graphPath, Map<String, Object> postData, SocialNetworkPostListener listener) {
		facebookFacade.post(context, graphPath, postData, listener);
	}

	@Override
	public void get(Activity context, String graphPath, Map<String, Object> postData, SocialNetworkPostListener listener) {
		facebookFacade.get(context, graphPath, postData, listener);
	}

	@Override
	public void delete(Activity context, String graphPath, Map<String, Object> postData, SocialNetworkPostListener listener) {
		facebookFacade.delete(context, graphPath, postData, listener);
	}

	@Override
	public byte[] getImageForPost(Activity context, Uri imagePath) throws IOException {
		return imageUtils.scaleImage(context, imagePath);
	}
	
	@Override
	public byte[] getImageForPost(Activity context, Bitmap image, CompressFormat format) throws IOException {
		return imageUtils.scaleImage(context, image, format);
	}

	public void setFacebookFacade(FacebookFacade facebookFacade) {
		this.facebookFacade = facebookFacade;
	}

	public void setImageUtils(ImageUtils imageUtils) {
		this.imageUtils = imageUtils;
	}
	
	public void setConfig(SocializeConfig config) {
		this.config = config;
	}
	
	public void setFacebookAuthProvider(EmptyAuthProvider p) {
		// This is just here so we don't get console warnings when we want to load the notifications context.
	}

	protected SocializeService getSocialize() {
		return Socialize.getSocialize();
	}
	
	void setFacebook(Facebook facebook) {
		this.facebook = facebook;
	}
}
