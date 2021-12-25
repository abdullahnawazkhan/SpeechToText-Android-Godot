<h1 align="center"><project-name>Speech To Text For Android</h1>

<p align="center"><project-description></p>

## How To Use
Open in android studio and press the build button. After you have built it out there should be an aab file out in your 
<project location>\SpeechToText\build\outputs\aar\

Take that and the supplied GDAP file in the root of the project and place it into your Godot androids build template plugins directory. 

Once you have that plugin installed then you need to go to Project -> Exports -> Android -> Plugins and enable it.

YOU MUST HAVE THE RECORD AUDIO PERMISSION

## API Refrence
- Sets the language of your text to speech plugin.
setLanguage(string Language) 

- Starts the process of listening to the user.
listen()

- Stops the listening and reports the audio it emits the listening_completed signel.
stop()

- Returns the last sessions words the user spoke.
getWords()

## Signels
listening_completed -> string
Emitted on completion of the users session with the voice to text returns voice data

error -> int
Emitted on error of voice processing with an error code.
## Future Updates


## 🤝 Support

Contributions, issues, and feature requests are welcome!

Give a ⭐️ if you like this project!