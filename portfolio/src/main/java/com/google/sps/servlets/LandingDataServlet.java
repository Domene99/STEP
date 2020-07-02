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

package com.google.sps.servlets;

import com.google.sps.classes.LandingSite;
import com.google.gson.Gson;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Returns landing site data as a JSON array, e.g. [{"lat": 38.4404675, "lng": -122.7144313, "missionNum": "13", "description": "landed on Feb. 32 2022"}] */
@WebServlet("/landing-data")
public class LandingDataServlet extends HttpServlet {

  private Collection<LandingSite> landingSites;
  private String payload;

  @Override
  public void init() {
    landingSites = new ArrayList<>();

    try (Scanner scanner = new Scanner(getServletContext().getResourceAsStream("/WEB-INF/landing-data.csv"))) {
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] cells = line.split(",");
      
            double lat = Double.parseDouble(cells[0]);
            double lng = Double.parseDouble(cells[1]);
            String missionNum = cells[2];
            String description = cells[3];
      
            landingSites.add(new LandingSite(lat, lng, missionNum, description));
          }
          scanner.close();

          payload = new Gson().toJson(landingSites);
    }catch (Exception e) {
        System.out.println(e);
    }
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");
    response.getWriter().println(payload);
  }
}