package com.ssrn.fake_old_platform;

class OverriddenLoggedInUserAccount {
    private String accountId;
    private String accountName;

     OverriddenLoggedInUserAccount() {
    }

     String getAccountId() {
        return accountId;
    }

     void setAccountId(String accountId) {
        this.accountId = accountId;
    }

     String getAccountName() {
        return accountName;
    }

     void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    void setAccountToDefault(){
         setAccountId("");
         setAccountName("");
    }
}
