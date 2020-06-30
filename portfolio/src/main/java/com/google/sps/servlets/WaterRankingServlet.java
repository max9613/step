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

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import java.util.ArrayList;

/** Servlet that returns water rankings.*/
@WebServlet("/water-rankings")
public class WaterRankingServlet extends HttpServlet {

  ArrayList<String> water_rankings = new ArrayList<String>();

  public WaterRankingServlet() {
      water_rankings.add("VOSS");
      water_rankings.add("FIJI");
      water_rankings.add("SMART WATER");
      water_rankings.add("CRYSTAL GEYSER");
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("text/html;");
    Gson gson = new Gson();
    String output = gson.toJson(getWaterRankings()); 
    response.getWriter().println(output);
  }

  private String[] getWaterRankings() {
      String[] rankings = new String[water_rankings.size()];
      for(int i = 0; i < rankings.length; i++) {
          rankings[i] = water_rankings.get(i);
      }
      return rankings;
  }
}
