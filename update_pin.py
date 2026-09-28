import re

with open('C:\\Users\\samsung\\Desktop\\Android Project\\estor\\app\\src\\main\\java\\estor\\app\\PinActivity.java', 'r', encoding='utf-8') as f:
    content = f.read()

old_method = """    private void checkFirstLaunch() {

        SharedPreferences preferences =
                getSharedPreferences(
                        PREF_NAME,
                        MODE_PRIVATE
                );


        String savedPin =
                preferences.getString(
                        KEY_PIN,
                        null
                );


        // =====================================================
        // FIRST TIME
        // =====================================================

        if (savedPin == null) {

            currentMode = MODE_SET_PIN;

            tvTitle.setText("Set PIN");

            tvSubtitle.setText(
                    "Create a 4-digit PIN"
            );


            // Hide Forgot PIN
            tvForgotPin.setVisibility(
                    View.GONE
            );

        }

        // =====================================================
        // EXISTING USER
        // =====================================================

        else {

            currentMode = MODE_LOGIN;

            tvTitle.setText("Enter PIN");

            tvSubtitle.setText(
                    "Enter your 4-digit PIN"
            );

            tvForgotPin.setVisibility(
                    View.VISIBLE
            );
        }
    }"""

new_method = """    private void checkFirstLaunch() {

        // ========================================================
        // CHECK STORE NAME FIRST
        // ========================================================

        SharedPreferences settingsPrefs =
                getSharedPreferences(
                        SettingsActivity.SETTINGS_PREFS,
                        MODE_PRIVATE
                );

        String storeName =
                settingsPrefs.getString(
                        SettingsActivity.KEY_STORE_NAME,
                        ""
                );


        // ========================================================
        // IF STORE NAME IS EMPTY (FIRST LAUNCH)
        // ========================================================

        if (storeName.isEmpty()) {

            // ========================================================
            // SHOW STORE NAME PROMPT
            // ========================================================

            showStoreNamePrompt();

            return;
        }


        // ========================================================
        // EXISTING PIN CHECK
        // ========================================================

        SharedPreferences preferences =
                getSharedPreferences(
                        PREF_NAME,
                        MODE_PRIVATE
                );


        String savedPin =
                preferences.getString(
                        KEY_PIN,
                        null
                );


        // =====================================================
        // FIRST TIME
        // =====================================================

        if (savedPin == null) {

            currentMode = MODE_SET_PIN;

            tvTitle.setText("Set PIN");

            tvSubtitle.setText(
                    "Create a 4-digit PIN"
            );


            // Hide Forgot PIN
            tvForgotPin.setVisibility(
                    View.GONE
            );

        }

        // =====================================================
        // EXISTING USER
        // =====================================================

        else {

            currentMode = MODE_LOGIN;

            tvTitle.setText("Enter PIN");

            tvSubtitle.setText(
                    "Enter your 4-digit PIN"
            );

            tvForgotPin.setVisibility(
                    View.VISIBLE
            );
        }
    }"""

if old_method in content:
    content = content.replace(old_method, new_method)
    with open('C:\\Users\\samsung\\Desktop\\Android Project\\estor\\app\\src\\main\\java\\estor\\app\\PinActivity.java', 'w', encoding='utf-8') as f:
        f.write(content)
    print('Replacement successful')
else:
    print('Old method not found')
    # Debug: find checkFirstLaunch
    idx = content.find('checkFirstLaunch')
    if idx > 0:
        print('Found at index:', idx)
        print(content[idx:idx+600])