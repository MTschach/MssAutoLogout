package de.mss.autologout.server.rest;

import java.text.SimpleDateFormat;

import de.mss.autologout.server.AutoLogoutWebService;
import de.mss.utils.exception.MssException;

public abstract class SecretAutoLogoutWebService extends AutoLogoutWebService {

	protected void checkSecret(String secret) throws MssException {
        byte[] s = new SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date()).getBytes();
        StringBuilder sb = new StringBuilder(s[3]);
        sb.append(s[5]);
        sb.append(s[7]);
        sb.append(s[9]);
        sb.append(s[11]);

        if (!sb.toString().equals(secret))
           throw new MssException(de.mss.utils.exception.ErrorCodes.ERROR_INVALID_PARAM, "invalid secret");
	}
}
