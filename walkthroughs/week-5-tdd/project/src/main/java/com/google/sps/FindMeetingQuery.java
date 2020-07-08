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

import com.google.common.collect.Iterables; 
import com.google.common.collect.Sets;
import java.lang.Math;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class FindMeetingQuery {
  /*
   * Function to return a collection of time ranges where people are available to meet.
   * Given a request with the minimum time for a meeting and a list of mandatory and optional attendees
   * it returns all time range ranges where all mandatory attendees can meet by discarding all time ranges
   * where there is an event involving an attendee. If there are time ranges where all mandatory attendees 
   * and optional attendees are available this gets returned.
   * 
   * Runtime Complexity: O(n*(n + q + k)) where 'n' is equals to the length of events
   * 'q' is equals to the length of the mandatory attendees and 'k' the length of optional
   * could be considered O(n^2) for simplicity
   * 
   * @param events    A collection of events which has attendees, our results should avoid these events
   * @param request   A specification on the minimum duration, mandatory and optional attendees
   * 
   * @return          A collection of time ranges where attendees are available
   * 
  */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    if (request.getAttendees().size() <= 0 && request.getOptionalAttendees().size() <= 0) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }  
    
    HashSet<String> mandatoryAttendees = new HashSet<String>(request.getAttendees());
    HashSet<String> optionalAttendees = new HashSet<String>(request.getOptionalAttendees());
    HashSet<String> allAttendees = Sets.newHashSet(Iterables.concat(mandatoryAttendees, optionalAttendees));
      
    String endOfDayTitle = "END_OF_DAY";
    String startOfDayTitle = "START_OF_DAY";
    Event endOfDay = new Event(endOfDayTitle, TimeRange.fromStartDuration(TimeRange.END_OF_DAY + 1, 0), allAttendees);
    Event startOfDay = new Event(startOfDayTitle, TimeRange.fromStartDuration(TimeRange.START_OF_DAY, 0), allAttendees);

    ArrayList<Event> allEvents = new ArrayList<Event>(events);

    allEvents.add(endOfDay);
    allEvents.add(startOfDay);
      
    allEvents.sort(Event.EVENT_COMPARATOR);

    ArrayList<Event> applicableEventsOnly = removeEventsWithNoAttendees(mandatoryAttendees, allEvents);
    ArrayList<Event> mandatoryNonOverlapping = sortedEventsIntoNonOverlappingSortedEvents(mandatoryAttendees, applicableEventsOnly);
    ArrayList<TimeRange> available = availableTimesFromSortedNonOverlappingEvents(mandatoryNonOverlapping, request);

    if (optionalAttendees.size() == 0) {
      return available;
    }

    ArrayList<Event> applicableEventsOnlyOfOptional = removeEventsWithNoAttendees(optionalAttendees, allEvents);
    ArrayList<Event> optionalNonOverlapping = sortedEventsIntoNonOverlappingSortedEvents(optionalAttendees, applicableEventsOnlyOfOptional);
    ArrayList<TimeRange> availableWithOptional = availableTimesFromSortedNonOverlappingEvents(optionalNonOverlapping, request);
    
    if (available.size() == 0) {
      return availableWithOptional;
    }
    
    ArrayList<TimeRange> finalTimeRanges = mergeTimeRangesThatIntersect(availableWithOptional, available);
    return finalTimeRanges;
  }
   
  /*
   * Function to merge time ranges that intersect between two lists of time ranges
   * 
   * Runtime: O(n*logm) where 'n' is the length of optional and 'm' is the length of mandatory
   * 
   * @param optional    A list of time ranges in which for every one we'll search for an intersection
   * @param mandatory   A list of time ranges to search within
   * 
   * return   A list of time ranges where all ranges are within both input lists
   * 
  */
  private ArrayList<TimeRange> mergeTimeRangesThatIntersect(ArrayList<TimeRange> optional, ArrayList<TimeRange> mandatory) {

    ArrayList<TimeRange> finalTimeRanges = new ArrayList<>();

    for (TimeRange optionalTimeRange: optional) {
      int start = optionalTimeRange.start();
      int end = optionalTimeRange.end();

      int indexOfIntersection = binarySearchOverlappingTimeRange(mandatory, 0, mandatory.size() - 1, start);

      if (indexOfIntersection != -1) {
        int mandatoryEnd = mandatory.get(indexOfIntersection).end();
        if (end <= mandatoryEnd) {
          finalTimeRanges.add(optionalTimeRange);
        }
      }
    }

    return finalTimeRanges.size() != 0 ? finalTimeRanges : mandatory;
  }
  
  /*
   * This function searches for the element closest and not greater than startOfOptional or equal
   * 
   * Runtime complexity: O(logn) where 'n' is the size of mandatory
   * 
   * @param mandatory       A list of mandatory time ranges in which to search
   * @param startIndex      The lowest index in which to search for
   * @param endIndex        The highest index in which to search for
   * @param startOfOptional The element to search for
  */
  private int binarySearchOverlappingTimeRange(ArrayList<TimeRange> mandatory, int startIndex, int endIndex, int startOfOptional) {
    int midIndex = -1;
    while (startIndex <= endIndex) {
      int startOfMandatory = mandatory.get(startIndex).start();
      
      if (startIndex == endIndex) {
        return startOfMandatory <= startOfOptional ? startIndex : -1;  
      }

      midIndex = startIndex + (endIndex - startIndex) / 2;
      
      if (startOfOptional < mandatory.get(midIndex).start()) {
        endIndex = midIndex;
      } else {
        startIndex = midIndex + 1;
      }
    }    

    return midIndex;
  }

  /*
   * Removes events in which no attendees intersect between the event and requested attendees
   * 
   * Runtime O(n*(n + q)) where 'n' is the length of events and 'q' is the length of the attendees
   * 
   * @param attendes    A hashset of all applicable attendees
   * @param events      A sorted list of the events to merge
   * 
   * return   A list of events where all events have at least one attendee from the set
   *  
  */
  private ArrayList<Event> removeEventsWithNoAttendees(HashSet<String> attendees, ArrayList<Event> events) {
    ArrayList<Event> sequentialEvents = new ArrayList<>(events);

    for (int i = 0; i < sequentialEvents.size(); ++i) { // n
      HashSet<String> attendeesForEvent = new HashSet<String>(sequentialEvents.get(i).getAttendees());

      attendeesForEvent.retainAll(attendees); // q

      if (attendeesForEvent.size() <= 0) {
        sequentialEvents.remove(i); // n
        --i;
      }
    }

    return sequentialEvents;
  }

  /*
   * Method to merge overlapping events from a sorted array
   * Ex:       |---A---|
   *                |--B--|
   * Becomes:  |----------| <- MERGED_EVENT 
   * 
   * Runtime: O(n^2) where 'n' is the length of the events
   *
   * @param attendes    A hashset of all applicable attendees
   * @param events      A sorted list of the events to merge
   * 
   * return   A list of sorted and non overlapping sorted events
   * 
  */
  private ArrayList<Event> sortedEventsIntoNonOverlappingSortedEvents(HashSet<String> attendees, ArrayList<Event> events) {
    ArrayList<Event> sequentialEvents = new ArrayList<>(events);    

    for (int i = 0; i < sequentialEvents.size() - 1;) { // n
      Event eventToCheck = sequentialEvents.get(i);
      Event nextEvent = sequentialEvents.get(i + 1);
      
      TimeRange eventTimeRange = eventToCheck.getWhen();
      TimeRange nextEventTimeRange = nextEvent.getWhen();

      if (eventTimeRange.overlaps(nextEventTimeRange)) {
        sequentialEvents.remove(i); // n
        sequentialEvents.remove(i); // n
        TimeRange mergedTimeRange = TimeRange.fromStartEnd(eventTimeRange.start(), Math.max(eventTimeRange.end(), nextEventTimeRange.end()), false);
        Event mergedEvent = new Event("MERGED_EVENT", mergedTimeRange, attendees);
        sequentialEvents.add(i, mergedEvent); // n
      } else {
        ++i;
      }
    }

    return sequentialEvents;
  }

  /*
   * Takes all gaps between events which are at least a certain duration and returns it
   * 
   * Runtime complexity: O(n^2) or O(n*k) where 'n' is the size of sequentialEvents,
   * 'k' is the size of availableTimes and can be approximated to 'n'
   * 
   * @param sequentialEvents  list of events that are sorted by time and non overlapping
   * @param request
   * 
   * return list of time ranges 
   * 
   */
  private ArrayList<TimeRange> availableTimesFromSortedNonOverlappingEvents (ArrayList<Event> sequentialEvents, MeetingRequest request) {
    ArrayList<TimeRange> availableTimes = new ArrayList<>();
    for (int i = 0; i < sequentialEvents.size() - 1; ++i) { // n
      
      Event eventToCheck = sequentialEvents.get(i);
      Event nextEvent = sequentialEvents.get(i + 1);
      
      int endTimeEvent = eventToCheck.getWhen().end();
      int startTimeNextEvent = nextEvent.getWhen().start();
      
      int duration = startTimeNextEvent - endTimeEvent;
      

      if (duration >= request.getDuration()) {
        availableTimes.add(TimeRange.fromStartDuration(endTimeEvent, duration)); // k
      }
    }
      
    return availableTimes;
  }
}
