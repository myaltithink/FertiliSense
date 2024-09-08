from datetime import datetime, timedelta
from typing import Any, Text, Dict, List
import firebase_admin
from firebase_admin import credentials, firestore
from rasa_sdk import Action, Tracker
from rasa_sdk.executor import CollectingDispatcher
from rasa_sdk.events import SlotSet
import wikipediaapi
import openai

# Initialize Firebase
cred = credentials.Certificate("fertilisense-f1335-firebase-adminsdk-erc4z-f6c9372187.json")
firebase_admin.initialize_app(cred)
db = firestore.client()

# Initialize Wikipedia API with a custom user agent
wiki_wiki = wikipediaapi.Wikipedia(
    language='en',
    user_agent='FertiliSense/1.0 (Mobile App; Contact: ybiza2018@gmail.com)'
)

# Set up OpenAI API key
openai.api_key = 'your_key'

class ActionLogMenstrualCycle(Action):
    def name(self) -> str:
        return "action_log_menstrual_cycle"

    def run(self, dispatcher: CollectingDispatcher, tracker: Tracker, domain: dict):
        start_date = tracker.get_slot("start_date")
        end_date = tracker.get_slot("end_date")
        cycle_duration = tracker.get_slot("cycle_duration")
        period_duration = tracker.get_slot("period_duration")

        # Debug logs
        print(f"DEBUG: Start Date Slot Value: {start_date}")
        print(f"DEBUG: End Date Slot Value: {end_date}")
        print(f"DEBUG: Cycle Duration Slot Value: {cycle_duration}")
        print(f"DEBUG: Period Duration Slot Value: {period_duration}")

        if not start_date or not end_date or not cycle_duration or not period_duration:
            dispatcher.utter_message(text="Please provide all required information: start date, end date, cycle duration, and period duration.")
            print("DEBUG: Missing slot value(s).")
            return []

        user_id = tracker.sender_id

        try:
            # Calculate dates
            start_date_dt = datetime.strptime(start_date, "%d/%m/%Y")
            end_date_dt = datetime.strptime(end_date, "%d/%m/%Y")
            cycle_duration_days = int(cycle_duration)
            period_duration_days = int(period_duration)

            print(f"DEBUG: Start Date (datetime): {start_date_dt}")
            print(f"DEBUG: End Date (datetime): {end_date_dt}")

            # Fetch previous cycles from Firestore
            previous_cycles = db.collection('menstrual_cycles').document(user_id).get().to_dict()
            if previous_cycles:
                historical_cycles = previous_cycles.get('cycles', [])
            else:
                historical_cycles = []

            # Add the current cycle
            historical_cycles.append({
                'start_date': start_date,
                'end_date': end_date,
                'cycle_duration': cycle_duration,
                'period_duration': period_duration
            })

            # Calculate strong flow durations based on historical data
            strong_flow_durations = []
            for cycle in historical_cycles:
                cycle_start = datetime.strptime(cycle['start_date'], "%d/%m/%Y")
                cycle_end = datetime.strptime(cycle['end_date'], "%d/%m/%Y")
                period_duration = (cycle_end - cycle_start).days + 1
                strong_flow_start = cycle_start + timedelta(days=(period_duration // 3))
                # Assuming we need to use the user's data to determine the end of the strong flow
                strong_flow_end = strong_flow_start + timedelta(days=min(2, period_duration // 3))  # Ensure the end is within the period duration
                strong_flow_duration = (strong_flow_end - strong_flow_start).days + 1
                strong_flow_durations.append(strong_flow_duration)

            # Average strong flow duration
            if strong_flow_durations:
                average_strong_flow_duration = sum(strong_flow_durations) / len(strong_flow_durations)
            else:
                average_strong_flow_duration = 2  # Default if no historical data

            # Calculate predictions based on historical data
            predictions = []
            current_start_date = start_date_dt

            # Determine the start date for prediction based on historical data
            if historical_cycles:
                last_cycle = historical_cycles[-1]
                last_start_date = datetime.strptime(last_cycle['start_date'], "%d/%m/%Y")
                current_start_date = last_start_date + timedelta(days=int(last_cycle['cycle_duration']))

            for _ in range(4):  # Predict for the next 4 cycles
                cycle_end_date_dt = current_start_date + timedelta(days=period_duration_days)
                cycle_end_date = cycle_end_date_dt.strftime("%d/%m/%Y")

                next_cycle_start_date_dt = current_start_date + timedelta(days=cycle_duration_days)
                next_cycle_start_date = next_cycle_start_date_dt.strftime("%d/%m/%Y")

                ovulation_date_dt = next_cycle_start_date_dt - timedelta(days=14)
                fertile_window_start_dt = ovulation_date_dt - timedelta(days=2)
                fertile_window_end_dt = ovulation_date_dt + timedelta(days=2)

                fertile_window_start = fertile_window_start_dt.strftime("%d/%m/%Y")
                fertile_window_end = fertile_window_end_dt.strftime("%d/%m/%Y")

                # Calculate predicted ovulation date
                ovulation_date = ovulation_date_dt.strftime("%d/%m/%Y")

                # Calculate strong flow for the current cycle
                strong_flow_start_dt = current_start_date + timedelta(days=(period_duration_days // 3))
                strong_flow_end_dt = strong_flow_start_dt + timedelta(days=int(average_strong_flow_duration) - 1)
                strong_flow_start = strong_flow_start_dt.strftime("%d/%m/%Y")
                strong_flow_end = strong_flow_end_dt.strftime("%d/%m/%Y")

                predictions.append({
                    'cycle_start_date': current_start_date.strftime("%d/%m/%Y"),
                    'cycle_end_date': cycle_end_date,
                    'fertile_window_start': fertile_window_start,
                    'fertile_window_end': fertile_window_end,
                    'next_cycle_start_date': next_cycle_start_date,
                    'ovulation_date': ovulation_date,
                    'strong_flow_start': strong_flow_start,
                    'strong_flow_end': strong_flow_end
                })

                # Prepare for next cycle prediction
                current_start_date = next_cycle_start_date_dt

            # Save to Firestore
            db.collection('menstrual_cycles').document(user_id).set({
                'cycles': historical_cycles,
                'predictions': predictions
            })
            dispatcher.utter_message(text="Your menstrual cycle information and predictions for the next four months have been recorded successfully.")
        except ValueError as e:
            dispatcher.utter_message(text="There was an error processing the dates. Please use the format dd/mm/yyyy.")
            print(f"ERROR: {e}")

        return []

class ActionGreet(Action):
    def name(self) -> str:
        return "action_greet"

    def run(self, dispatcher: CollectingDispatcher, tracker: Tracker, domain: dict):
        dispatcher.utter_message(text="Hello! How can I help you today?")
        return []

class ActionGoodbye(Action):
    def name(self) -> str:
        return "action_goodbye"

    def run(self, dispatcher: CollectingDispatcher, tracker: Tracker, domain: dict):
        dispatcher.utter_message(text="Goodbye! Have a great day!")
        return []

class ActionHandleMood(Action):
    def name(self) -> str:
        return "action_handle_mood"

    def run(self, dispatcher: CollectingDispatcher, tracker: Tracker, domain: dict):
        dispatcher.utter_message(text="It sounds like you're not feeling great. Do you want to talk about it?")
        return []

class ActionQueryWikipedia(Action):
    def name(self) -> str:
        return "action_query_wikipedia"

    def run(self, dispatcher: CollectingDispatcher, tracker: Tracker, domain: dict):
        topic = tracker.latest_message.get('text')
        if 'male reproductive system' in topic.lower():
            page = wiki_wiki.page('Male_reproductive_system')
        elif 'female reproductive system' in topic.lower():
            page = wiki_wiki.page('Female_reproductive_system')
        else:
            dispatcher.utter_message(text="I'm sorry, I don't have information on that topic.")
            return []

        response = page.summary[:500]  # Return a short summary
        dispatcher.utter_message(text=response)
        return []

class ActionQueryGPT4(Action):
    def name(self) -> str:
        return "action_query_gpt4"

    def run(self, dispatcher: CollectingDispatcher, tracker: Tracker, domain: dict):
        topic = tracker.latest_message.get('text')
        prompt = f"Provide detailed information on the following topic: {topic}"

        response = openai.ChatCompletion.create(
            model="gpt-3.5-turbo",
            messages=[
                {"role": "system", "content": "You are a helpful assistant."},
                {"role": "user", "content": prompt}
            ],
            max_tokens=150
        )

        result = response['choices'][0]['message']['content'].strip()
        dispatcher.utter_message(text=result)
        return []

class ActionAskDay(Action):
    def name(self) -> Text:
        return "action_ask_day"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_day")

        return []

class ActionHeadSymptom(Action):
    def name(self) -> Text:
        return "action_head_symptom"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_head_symptom")

        return []

class ActionHeadWhole(Action):
    def name(self) -> Text:
        return "action_head_whole"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_head_whole_response")

        return []

class ActionHeadSides(Action):
    def name(self) -> Text:
        return "action_head_sides"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_head_sides_response")

        return []

class ActionHeadCrown(Action):
    def name(self) -> Text:
        return "action_head_crown"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_head_crown_response")

        return []

class ActionHeadEyes(Action):
    def name(self) -> Text:
        return "action_head_eyes"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_head_eyes_response")

        return []

class ActionFluSymptom(Action):
    def name(self) -> Text:
        return "action_flu_symptom"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_flu_response")

        return []

class ActionVisionSymptom(Action):
    def name(self) -> Text:
        return "action_vision_symptom"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_vision_types")

        return []

class ActionStomachSymptom(Action):
    def name(self) -> Text:
        return "action_stomach_symptom"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_stomach_types")

        return []


