// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.data;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;

public class RequestHandler {

  // Used when form value is a single value. For example: getParameterOrThrow(request, "email"), where the result returned would be the valued entered in the form.
  public static String getParameterOrThrow(HttpServletRequest request, String formKey) {
    String result = request.getParameter(formKey);
    if(result == null || result.isEmpty()) {
      throw new IllegalArgumentException("Form value cannot be null");
    }
    return result;
  }

  // Used when form value is a list. For example: used when multiple entries with ending "-from-times" is passed in from the request form.
  public static ArrayList<String> getParameterValuesOrThrow(HttpServletRequest request, String formKey){
    System.out.println("Form Key is: " + formKey);
    ArrayList<String> results = new ArrayList<String>(Arrays.asList(request.getParameterValues(formKey)));
    if(results.isEmpty() || results == null) {
      throw new IllegalArgumentException("Form value cannot be null");
    }

    // Checks if there is a value that is empty which means a blank time range was submitted
    for(int i = 0; i < results.size(); i++) {
      if(results.get(i).equals("")) {
        throw new IllegalArgumentException("Form value cannot be null");
      }
    }
    return results;
  }
}
