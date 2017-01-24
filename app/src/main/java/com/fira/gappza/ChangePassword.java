package com.fira.gappza;

/**
 * Created by firdavsiimajidzoda on 11/30/16.
 */

public class ChangePassword {

    //region Fields
    String currentPassword;
    String newPassword;
    String confirmNewPassword;
    //endregion

    // Constructor
    ChangePassword(){
        currentPassword = "";
        newPassword = "";
        confirmNewPassword = "";
    }

    //region Validations
    /**
     * Validate Current Password
     * @return "" if password format is valid, error message otherwise
     */
    public String validateCurrentPassword() {
        if (currentPassword.matches("")){
            return "Your current password can not be empty";
        } else {
            return "";
        }
    }

    /**
     * Validate New Password
     * @return "" if password format is valid, error message otherwise
     */
    public String validateNewPassword() {
        if (newPassword.matches("")) {
            return "Your password can not be empty.";
        } else {
            if (newPassword.length() < 8 || newPassword.length() > 14) {
                return "Your password must be between 8 to 14 characters.";
            } else {
                return "";
            }
        }
    }

    /**
     * Validate Confirm Password, if it maches new Password
     * @return "" if password format is valid, error message otherwise
     */
    public String validateConfirmNewPassword() {
        if (confirmNewPassword.matches(newPassword)) {
            return "";
        } else {
            return "Your password do not match.";
        }
    }

    /**
     * Valides if Change Password matches requirements
     * @return
     */
    public String validateChangePassword() {
        return validateCurrentPassword()+validateNewPassword()+validateConfirmNewPassword();
    }
    //endregion
}
