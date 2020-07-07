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

import com.google.sps.classes.ChartDataPoint;
import com.google.gson.Gson;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Scanner;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// Returns budget-patent data of NASA as a JSON array, e.g. [{"year": 1900, "budget": 20, "patents": 2}]
@WebServlet("/nasa-data")
public class ChartServlet extends HttpServlet {

  private Collection<ChartDataPoint> dataPoints;
  private String chartData;
  private HashMap<Integer, Integer> patentsPerYear;

  @Override
  public void init() {
    dataPoints = new ArrayList<>();
    patentsPerYear = new HashMap<Integer, Integer>();

    try (Scanner scanner = new Scanner(getServletContext().getResourceAsStream("/WEB-INF/patents.csv"))) {
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] cells = line.split(",");

            int year = Integer.parseInt(cells[5]);
            patentsPerYear.compute(year, (key, val) -> val == null ? 1 : val + 1);  
          }
    }

    try (Scanner scanner = new Scanner(getServletContext().getResourceAsStream("/WEB-INF/budget.csv"))) {
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] cells = line.split(",");

            int year = Integer.parseInt(cells[0]);
            int budgetOfYear = Integer.parseInt(cells[1]);
                
            int patents = patentsPerYear.getOrDefault(year, 0);
	    
            dataPoints.add(new ChartDataPoint(year, budgetOfYear, patents));
        }
    }

    chartData = new Gson().toJson(dataPoints);
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");
    response.getWriter().println(chartData);
  }
}
