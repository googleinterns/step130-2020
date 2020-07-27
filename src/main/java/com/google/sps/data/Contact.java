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

public class Contact {
  // Represents contact email.
  private String email;
  // Represents contact phone number.
  private String phone;
  // Represents contact first name.
  private String firstName;
  // Represents contact last name.
  private String lastName;
  // Represents contact's pronouns.
  private String pronouns;

  public Contact(String email, String phone, String firstName, String lastName, String pronouns) {
    this.email = email;
    this.phone = phone;
    this.firstName = firstName;
    this.lastName = lastName;
    this.pronouns = pronouns;
  }
}
