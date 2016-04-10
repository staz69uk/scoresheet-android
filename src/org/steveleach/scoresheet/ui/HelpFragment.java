/*  Copyright 2016 Steve Leach

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
package org.steveleach.scoresheet.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import java.util.Locale;

import org.steveleach.ihscoresheet.*;

/**
 * Implementation code for the new Help UI fragment.
 *
 * @author Steve Leach
 */
public class HelpFragment extends Fragment {
    private View view;
    public static final String DEFAULT_LANGUAGE = "en";
    private String[] supportedLanguages = {DEFAULT_LANGUAGE};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.helpfragment, container, false);

        WebView webView = (WebView) view.findViewById(R.id.helpWebView);
        webView.loadUrl("file:///android_asset/html/help-"+getSupportedLanguage()+".html");

        return view;
    }

    /**
     * Returns the current device default language, if it is supported.
     * Returns "en" otherwise.
     */
    private String getSupportedLanguage() {
        String language = Locale.getDefault().getLanguage();

        boolean isSupported = false;
        for (String lang : supportedLanguages) {
            if (lang.equals(language)) {
                isSupported = true;
            }
        }
        if (! isSupported) {
            language = DEFAULT_LANGUAGE;
        }
        return language;
    }
}
