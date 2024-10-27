/*  AccountController
 *
 *  Copyright (C) 2023  Robert Schoech
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ims;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;

public class AccountController {

    private Account account;
    
    @FXML
    private Button btLogin;
    
    @FXML
    private Button btLogout;

    @FXML
    private Button btSignUp;

    @FXML
    private Label lbLoginMessage;

    @FXML
    private Label lbSignUpMessage;

    @FXML
    private PasswordField pfLoginPassword;

    @FXML
    private PasswordField pfSignUpConfirmPassword;

    @FXML
    private PasswordField pfSignUpPassword;
     
    @FXML
    private TabPane tabPane;

    @FXML
    private TextField tfSignUpEmail;

    @FXML
    private TextField tfUsername;
 
    @FXML
    private void initialize() throws Exception {
        // Initialisiere Account und Datenbank
        account = new Account();
    }   

    @FXML
private void onSignUp(ActionEvent event) throws Exception {
    // Überprüfe Benutzername, Passwort und bestätigtes Passwort
    String name = tfSignUpEmail.getText();
    if (name.isEmpty()) {
        lbSignUpMessage.setText("Type in email");
        return;
    }

    String pw = pfSignUpPassword.getText().trim();
    if (pw.equals("")) {
        lbSignUpMessage.setText("Enter a plausible password");
        return;
    }

    if (!pw.equals(pfSignUpConfirmPassword.getText())) {
        lbSignUpMessage.setText("Password and confirmed password are not identical");
        return;
    }

    // Überprüfe, ob der Benutzer bereits existiert (Einmalige Überprüfung hier im Controller)
    if (account.verifyAccount(name)) {
        lbSignUpMessage.setText("Email " + name + " has already an account");
        return;
    }
    
    // Füge neuen Benutzer hinzu, ohne weitere Überprüfung innerhalb von addAccount
    account.addAccount(name, pw);
    
    // Bestätigungsnachricht und Tabwechsel nach erfolgreicher Registrierung
    lbSignUpMessage.setText("Registration successful. Welcome!");
    
    // Wähle den Tab 'Log In'
    tabPane.getTabs().get(0).setDisable(true);
    
    // Reset Login und SignUp Felder
    resetLogin();
    resetSignup();
    
    // Wähle den Tab 'Log in'
    tabPane.getSelectionModel().select(1);
}


    @FXML
    private void onLogin(ActionEvent event) {
        String name = tfUsername.getText();
        String pw = pfLoginPassword.getText();
                        
        if (account.verifyPassword(name, pw)) {
            tabPane.getTabs().get(0).setDisable(true);
            tabPane.getTabs().get(1).setDisable(true);
            tabPane.getTabs().get(2).setDisable(false);
            tabPane.getSelectionModel().select(2);
        } else {
            lbLoginMessage.setText("'Email' or 'Password' are wrong");
            tabPane.getTabs().get(0).setDisable(false);
        }
    }
   
    @FXML
    private void onLogout(ActionEvent event) {
        // Setze Tabs zurück
        tabPane.getTabs().get(0).setDisable(false);
        tabPane.getTabs().get(1).setDisable(false);
        tabPane.getTabs().get(2).setDisable(true);
        
        // Setze Login zurück und wähle den Tab 'Log in'
        resetLogin();   
        tabPane.getSelectionModel().select(1);      
    }
    
    private void resetLogin() {
        tfUsername.setText("");
        pfLoginPassword.setText("");
        lbLoginMessage.setText("Login with your account");
    } 

    private void resetSignup() {
        tfSignUpEmail.setText("");
        pfSignUpPassword.setText("");
        pfSignUpConfirmPassword.setText("");
        lbSignUpMessage.setText("Create Account");
    }
}
