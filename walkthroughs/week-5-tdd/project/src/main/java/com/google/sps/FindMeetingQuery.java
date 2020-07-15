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

package com.google.sps;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collections;   
import java.util.List;


public final class FindMeetingQuery {
  /* 
    Finds optimal time ranges by first finding the set of ranges that fits all the mandatory attendees using the split range function on each event and then it finds
    the largest set of ranges that accommodate the maximum number of optional attendees, by performing the split range search on members of the power set of the optional
    attendees, in descending order of cardinality.
  */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
      ArrayList<TimeRange> returnRanges = new ArrayList<TimeRange>();
      if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
          return returnRanges;
      }
      returnRanges.add(TimeRange.WHOLE_DAY);
      // For mandatory attendees.
      for (Event event : events) {
          HashSet<String> intersection = new HashSet<String>(request.getAttendees());
          intersection.retainAll(event.getAttendees());
          if (intersection.size() > 0) {
              ArrayList<TimeRange> newRanges = new ArrayList<TimeRange>();
              for (TimeRange range : returnRanges) {
                  ArrayList<TimeRange> retrievedRanges = splitRange(range, event.getWhen(), (int) request.getDuration());
                  newRanges.addAll(retrievedRanges);
              }
              returnRanges = newRanges;
          }
      }
      // For optional attendees.
      if (request.getOptionalAttendees().size() > 0) {
        HashSet<HashSet<String>> attendeeConfigs = new HashSet<HashSet<String>>();
        attendeeConfigs.add(new HashSet<String>(request.getOptionalAttendees()));
        ArrayList<TimeRange> copyRanges = getOptimalOptionalRanges(events, attendeeConfigs, returnRanges, (int) request.getDuration());
        if (copyRanges != null && copyRanges.size() > 0) {
            returnRanges = copyRanges;
        } else if (request.getAttendees().size() == 0) {
            return new ArrayList<TimeRange>();
        }
      }
      Collections.sort(returnRanges, TimeRange.ORDER_BY_START);
      return returnRanges;
  }

  /*
    Finds the set of time ranges that contains the most possible times while also including as many optional members as possible.
  */
  public ArrayList<TimeRange> getOptimalOptionalRanges(Collection<Event> events, HashSet<HashSet<String>> attendeeConfigs, ArrayList<TimeRange> currentRanges, int duration) {
      HashSet<HashSet<String>> newConfigs = new HashSet<HashSet<String>>();
      ArrayList<TimeRange> returnRange = null;
      for (HashSet<String> config : attendeeConfigs) {
        if (config.size() > 0) {
            ArrayList<TimeRange> configRanges = getPossibleRanges(events, config, currentRanges, duration);
            if (returnRange == null && configRanges.size() > 0) {
                returnRange = configRanges;
            } else if (returnRange != null && returnRange.size() < configRanges.size()) {
                returnRange = configRanges;
            }
            aggregateLowerCardinalitySubsets(config, newConfigs);
        } else {
            return null;
        }
      }
      if (returnRange != null) {
          return returnRange;
      } else {
          return getOptimalOptionalRanges(events, newConfigs, currentRanges, duration);
      }
  }

  /* 
    Creates the members of the powerset of the passed set, that have a cardinality equal to the cardinality of the passed set minus one. 
  */
  public void aggregateLowerCardinalitySubsets(HashSet<String> elements, HashSet<HashSet<String>> targetSet) {
      for (String element : elements) {
          HashSet<String> newGroup = new HashSet<String>(elements);
          newGroup.remove(element);
          targetSet.add(newGroup);
      }
  }

  /*
    Uses the split function to find the set of time ranges with the largest cardinality that accommodates all the attendees.
  */
  public ArrayList<TimeRange> getPossibleRanges(Collection<Event> events, HashSet<String> attendees, ArrayList<TimeRange> currentRanges, int duration) {
      ArrayList<TimeRange> returnRanges = currentRanges;
      for (Event event : events) {
          HashSet<String> intersection = new HashSet<String>(attendees);
          intersection.retainAll(event.getAttendees());
          if (intersection.size() > 0) {
              ArrayList<TimeRange> newRanges = new ArrayList<TimeRange>();
              for (TimeRange range : returnRanges) {
                  ArrayList<TimeRange> retrievedRanges = splitRange(range, event.getWhen(), duration);
                  newRanges.addAll(retrievedRanges);
              }
              returnRanges = newRanges;
          }
      }
      return returnRanges;
  }

  /*
    Takes an existing time range and returns the portions of it that are not overlapping with the new time range. This can either result in two time ranges
    if the new range is wholly contained by the existing range, one time range if only part of the new range overlaps with the current range without consuming it,
    and zero if the current range is completely contained in the new range, or the candidate return ranges have less than the target duration.
  */
  private ArrayList<TimeRange> splitRange(TimeRange currentRange, TimeRange newRange, int targetDuration) {
      ArrayList<TimeRange> returnRanges = new ArrayList<TimeRange>();
      if (currentRange.start() > newRange.start()) {
          if (currentRange.end() > newRange.end() && currentRange.start() <= newRange.end()) {
              int revisedDuration = currentRange.end() - newRange.end();
              if (revisedDuration >= targetDuration) {
                  TimeRange revisedRange = TimeRange.fromStartDuration(newRange.end(), revisedDuration);
                  returnRanges.add(revisedRange);
              }
          } else if (currentRange.start() > newRange.end()) {
              returnRanges.add(currentRange);
          }
      } else if (currentRange.end() < newRange.end()) {
          if (currentRange.start() < newRange.start() && currentRange.end() >= newRange.start()) {
              int revisedDuration = newRange.start() - currentRange.start();
              if (revisedDuration >= targetDuration) {
                TimeRange revisedRange = TimeRange.fromStartDuration(currentRange.start(), revisedDuration);
                returnRanges.add(revisedRange);
              }
          } else if (currentRange.end() < newRange.start()) {
              returnRanges.add(currentRange);
          }
      } else if (currentRange.start() <= newRange.start() && currentRange.end() >= newRange.end()) { 
          int revisedDuration1 = newRange.start() - currentRange.start();
          int revisedDuration2 = currentRange.end() - newRange.end();
          if (revisedDuration1 >= targetDuration) {
            TimeRange revisedRange1 = TimeRange.fromStartDuration(currentRange.start(), revisedDuration1);
            returnRanges.add(revisedRange1);
          }
          if (revisedDuration2 >= targetDuration) {
            TimeRange revisedRange2 = TimeRange.fromStartDuration(newRange.end(), revisedDuration2);
            returnRanges.add(revisedRange2);
          }
      }
      return returnRanges;
  }
}
