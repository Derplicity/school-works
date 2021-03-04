/*
 * CS3210 - Principles of Programming Languages - Fall 2020
 * Instructor: Thyago Mota
 * Description: Prg04 - Publish Subscribe Simulation
 * Student(s) Name(s): Michael Kerl
 */

package main

import (
	"fmt"
	"math/rand"
	"sync"
	"time"
)

// A PubSub is a communication structure for publishers to publish topic messages to their
// subscribers
type PubSub struct {
	mu     sync.Mutex
	topics map[string][]chan string
}

var wg sync.WaitGroup

// Creates and returns a new channel on a given topic, updating the PubSub struct
func (ps *PubSub) subscribe(topic string) chan string {
	topicChannel := make(chan string)
	ps.mu.Lock()
	ps.topics[topic] = append(ps.topics[topic], topicChannel)
	ps.mu.Unlock()
	return topicChannel
}

// Writes the given message on all the channels associated with the given topic
func (ps *PubSub) publish(topic string, msg string) {
	ps.mu.Lock()
	defer ps.mu.Unlock()
	for i := 0; i < len(ps.topics[topic]); i++ {
		ps.topics[topic][i] <- msg
	}
}

// Closes all the channels associated with the given topic
func (ps *PubSub) close(topic string) {
	ps.mu.Lock()
	defer ps.mu.Unlock()
	for i := 0; i < len(ps.topics[topic]); i++ {
		close(ps.topics[topic][i])
	}
}

// Sends messages taken from a given array of message, one at a time and at random intervals, to
// all topic subscribers
func publisher(ps *PubSub, topic string, msgs []string) {
	defer wg.Done()
	for i := 0; i < len(msgs); i++ {
		// 1-3 seconds between messages being published
		time.Sleep(time.Duration(rand.Intn(10000-1000)+1000) * time.Millisecond)
		ps.publish(topic, msgs[i])
	}
	ps.close(topic)
}

// Reads and displays all messages received from a particular topic
func subscriber(ps *PubSub, name string, topic string) {
	defer wg.Done()
	topicChannel := ps.subscribe(topic)
	for {
		msg, ok := <-topicChannel
		if !ok {
			break
		}
		fmt.Printf("%s received: %s\n", name, msg)
	}
}

// Creates publishers who send out topic messages;
// Creates subscribers who read the topic messages sent by the publishers they are subscribed to
func main() {
	ps := PubSub{topics: make(map[string][]chan string)}

	// Publisher topics and their associated messages
	pubInfo := map[string][]string{
		"Dogs": {
			"The Beatles song 'A Day in the Life' has a frequency only dogs can hear.",
			"Three dogs survived the Titanic sinking.",
			"A Bloodhound's sense of smell can be used as evidence in court.",
			"The tallest dog in the world is 44 inches tall.",
			"A Greyhound could beat a Cheetah in a long distance race.",
			"30% of Dalmatians are deaf in one ear.",
			"Dogs have three eyelids.",
			"Basenji dogs don't bark, they yodel.",
		},
		"Cats": {
			"The first year of a cat's life is equal to the first 15 years of a human life.",
			"Cats can rotate their ears 180 degrees.",
			"The hearing of the average cat is at least five times better than that of a human adult.",
			"Domestic cats speed about 70 percent of the day sleeping.",
			"A cat cannot see directly under its nose.",
			"It's not uncommon for cats to have extra toes.",
			"Meows are not innate cat language; they developed them to communicate with humans!",
		},
		"Birds": {
			"Ravens are great at mimicking human speech and sounds.",
			"Cardinals like to cover themselves in ants.",
			"Some Ducks sleep with one eye open.",
			"Most Hummingbirds weigh less than a Nickel",
			"In Ancient Greece, Pigeons delivered the results of the Olympic Games.",
			"Budgies catch each other's yawns.",
		},
	}

	// Subscriber names and their associated topics
	subInfo := map[string][]string{
		"Mary":  {"Dogs"},
		"Tom":   {"Dogs", "Cats"},
		"Jack":  {"Cats", "Birds"},
		"Sammy": {"Birds"},
	}

	// Create the publisher goroutines
	for topic, msgs := range pubInfo {
		wg.Add(1)
		go publisher(&ps, topic, msgs)
	}

	// Create the subscriber goroutines
	for name, topics := range subInfo {
		for _, topic := range topics {
			wg.Add(1)
			go subscriber(&ps, name, topic)
		}
	}

	// Wait for all publishers and subscribers to finish
	wg.Wait()
}
