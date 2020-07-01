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

package com.google.sps.classes;

/** Represents a landing sight. */
public class LandingSight {
  private final double lat;
  private final double lng;
  private final String missionNum;
  private final String description;

  public LandingSight(double lat, double lng, String missionNum, String description) {
    this.lat = lat;
    this.lng = lng;
    this.missionNum = missionNum;
    this.description = description;
  }
}