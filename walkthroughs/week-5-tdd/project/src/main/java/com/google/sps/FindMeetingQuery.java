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
import java.util.Set;


public final class FindMeetingQuery {

  /* 
    Finds optimal time ranges by first finding the set of ranges that fits all the mandatory attendees using the split range function on each event and then it finds
    the largest set of ranges that accommodate the maximum number of optional attendees, by performing the split range search on members of the power set of the optional
    attendees, in descending order of cardinality.
  */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
      List<TimeRange> returnRanges = new ArrayList<TimeRange>();
      List<Event> sortedEvents = new ArrayList<Event>(events);
      Collections.sort(sortedEvents, new EventComparator());
      if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
          return returnRanges;
      }
      // For mandatory attendees.
      returnRanges = getPossibleRanges(sortedEvents, new HashSet<String>(request.getAttendees()), (int) request.getDuration());
      // For optional attendees.
      if (request.getOptionalAttendees().size() > 0) {
        Set<Set<String>> attendeeConfigs = new HashSet<>();
        attendeeConfigs.add(new HashSet<String>(request.getOptionalAttendees()));
        List<TimeRange> copyRanges = getOptimalOptionalRanges(sortedEvents, attendeeConfigs, new HashSet<String>(request.getAttendees()), (int) request.getDuration());
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
  public List<TimeRange> getOptimalOptionalRanges(List<Event> events, Set<Set<String>> attendeeConfigs, Set<String> mandatoryAttendees, int duration) {
      Set<Set<String>> newConfigs = new HashSet<>();
      List<TimeRange> returnRange = null;
      for (Set<String> config : attendeeConfigs) {
        if (config.size() > 0) {
            Set<String> configCopy = new HashSet<>(config);
            configCopy.addAll(mandatoryAttendees);
            List<TimeRange> configRanges = getPossibleRanges(events, configCopy, duration);
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
          return getOptimalOptionalRanges(events, newConfigs, mandatoryAttendees, duration);
      }
  }

  /* 
    Creates the members of the powerset of the passed set, that have a cardinality equal to the cardinality of the passed set minus one. 
  */
  public void aggregateLowerCardinalitySubsets(Set<String> elements, Set<Set<String>> targetSet) {
      for (String element : elements) {
          Set<String> newGroup = new HashSet<String>(elements);
          newGroup.remove(element);
          targetSet.add(newGroup);
      }
  }

  /* 
    Takes the provided set of events and combines bordering events based on the provided list of attendees.
  */
  public List<Event> getMergedEvents(Collection<Event> events, Set<String> attendees) {
      List<Event> mergedEvents = new ArrayList<>();
      for (Event event : events) {
          if (!Collections.disjoint(event.getAttendees(), attendees)) {
              if (mergedEvents.size() > 0) {
                  Event lastEvent = mergedEvents.remove(mergedEvents.size() - 1);
                  if (event.getWhen().start() <= lastEvent.getWhen().end()) {
                      if (event.getWhen().end() > lastEvent.getWhen().end()) {
                          boolean inclusive = event.getWhen().end() == TimeRange.END_OF_DAY;
                          TimeRange newDuration = TimeRange.fromStartEnd(lastEvent.getWhen().start(), event.getWhen().end(), inclusive);
                          mergedEvents.add(new Event(lastEvent.getTitle(), newDuration, lastEvent.getAttendees()));
                      } else {
                          mergedEvents.add(lastEvent);
                      }
                  } else {
                      mergedEvents.add(lastEvent);
                      mergedEvents.add(event);
                  }
              } else {
                  mergedEvents.add(event);
              }
          }
      }
      return mergedEvents;
  }

  /*
    Uses the split function to find the set of time ranges with the largest cardinality that accommodates all the attendees.
  */
  public List<TimeRange> getPossibleRanges(Collection<Event> events, Set<String> attendees, int duration) {
      List<TimeRange> returnRanges = new ArrayList<TimeRange>();
      List<Event> mergedEvents = getMergedEvents(events, attendees);
      int beginningOfRange = TimeRange.START_OF_DAY;
      int endOfRange = TimeRange.START_OF_DAY;
      for (Event event : mergedEvents) {
          if (event.getWhen().start() - beginningOfRange >= duration) {
              returnRanges.add(TimeRange.fromStartDuration(beginningOfRange, event.getWhen().start() - beginningOfRange));
          }
          beginningOfRange = event.getWhen().end();
          endOfRange = event.getWhen().end();
      }
      if (TimeRange.END_OF_DAY - endOfRange >= duration) {
          returnRanges.add(TimeRange.fromStartEnd(endOfRange, TimeRange.END_OF_DAY, true));
      } else if (beginningOfRange == TimeRange.START_OF_DAY) {
          returnRanges.add(TimeRange.WHOLE_DAY);
      }
      return returnRanges;
  }
}