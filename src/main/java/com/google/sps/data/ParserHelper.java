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

import java.util.ArrayList;
import com.google.appengine.api.datastore.EmbeddedEntity;

public class ParserHelper {

  // Helper function for constructing hour ranges to allow multiple ranges for single dates.
  public static ArrayList<EmbeddedEntity> createHoursFromAndHoursToPairs(ArrayList<String> dayOptionFromTimes, ArrayList<String> dayOptionToTimes) {
    ArrayList<EmbeddedEntity> pairs = new ArrayList<EmbeddedEntity>();
    if(dayOptionFromTimes.size() != dayOptionToTimes.size()) {
      throw new IllegalArgumentException("Form value cannot be null");
    }
    for(int i = 0; i < dayOptionFromTimes.size(); i++) {
      EmbeddedEntity fromToPair = new EmbeddedEntity();
      fromToPair.setProperty("from", dayOptionFromTimes.get(i));
      fromToPair.setProperty("to", dayOptionToTimes.get(i));
      pairs.add(fromToPair);
    }
    return pairs;
  }
}
