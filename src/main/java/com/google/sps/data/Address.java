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

/*All addresses used in this application are in United States. */

public class Address {
  /* Represents Street Address, including street name, House number/Apartment/Suite/Room number (if any).
   *     Example:
   *         1600 Amphitheatre Parkway
   *         Mountain View, CA 94043
   *   +-------------------------------------------+
   *   | streetAddress | 1600 Amphitheatre Parkway |
   *   |          city | Mountain View             |
   *   |         state | CA                        |
   *   |       zipcode | 94043                     |
   *   +-------------------------------------------+
   */
  private String streetAddress;
  // Represents City name.
  private String city;
  // Represents the state name.
  private String state;
  // Represents the five digit zipcode.
  private String zipcode;

  public Address(String streetAddress, String city, String state, String zipcode) {
    this.streetAddress = streetAddress;
    this.city = city;
    this.state = state;
    this.zipcode = zipcode;
  }

}
