package com.kryptoeuro.accountmapper.domain;

import com.codeborne.security.mobileid.MobileIDSession;

public class PendingMobileIdAuthorisation {

	public MobileIDSession mobileIdSession;
	public String address;

	public PendingMobileIdAuthorisation(MobileIDSession mobileIdSession, String address) {
		this.mobileIdSession = mobileIdSession;
		this.address = address;
	}
}
