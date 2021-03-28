# VLC-Navigation for Android
## Presentation
VLC-Navigation is an application that allows the user to locate in real-time registered devices on a map. The localisation is based on Visible Light Communication technology and trilateration.

This project is conducted by ISEP students as an end-of-track project, under the supervision of Prof. Xun Zhang.

## Features
The application includes three menus, each associated with a main feature managed by the application.
### Displaying a map
The main feature of VLC technology this application highlights is a high precision and very fast indoor geolocation.

Agents navigating in the managed area will be detected and displayed on the indoor map of the building. The detection of the agents depends on the reception of visible light by a photoreceptor attached to a recording device. In our Proof-of-Concept demonstration, agents are either a rover with a Raspberry Pi attached, or a person with an Android device. The location of the agent is calculated using trilateration.

The indoor map of the building is generated from the given data. To see more about the generation of the map, see [the settings menu](#managing-the-application-settings).

On top of the map, markers representing registered lights and users allows navigating user to look for other people and find its way through the building. Lights are represented by a purple floating circle, and users are represented by a orange floating circle.

### Fast Fourier Transform
This menu displays the monitored data. Fast Fourier Transform (FFT) is displayed here. The graph represents amplitude as a function of frequencies of detected lights. As lights blink at different frequencies, we expect to detect multiple peaks.

By analysing the amplitude of the detected signals, we can calculate the distance from the photoreceptor to the agent.

### Managing the application settings
To make the application versatile, users can and have to manage the application settings: that is the list of storey in the building and the list of lights in the building.

Storeys are determined by:
- Their level (i.e: integers like 0, 1, 10, or negative integers like -1, -3)
- Their description (for example: "Administration floor", "Reception")
- Their blueprint. Blueprints are `.svg` files representing rooms of the floor.

Lights are determined by:
- Their position on the storey. The position is composed of a X:Y pair
- Their description
- The storey level they are located on
- The characteristic wavelength of the light's blinking
- The distance to the agent's photoreceptor.

Users can freely add storeys and lights to the list of lights. Users can also import a `.json` file to directly add lights and storeys to the application. However, in case a user imports data from a `.json` file, they will have to manually associate storey to blueprints. This cannot be bypassed, as Android's data access framework consider this a security breach.

Expected architecture of a `.json` data file:
``` JSON
{
    "floors":[
        {
            "description":"RDC",
            "filePath":"This is template data - please select an .SVG file",
            "order":0
        }, ...
    ],
    "lights":[
        {
            "description":"Light in the corridor #1",
            "distance":20.0,
            "floor":{
                "description":"RDC",
                "filePath":"This is template data - please select an .SVG file",
                "order":0
            },
            "lambda":0.0,
            "posX":3.0,
            "posY":2.0
        }, ...
    ]
}
```

## Accessibility
We tried our best to take into account handicaps. However, as very inexperimented developers, we are only aware of the most well-known disabilities. If you would like to give us some feedback or pointers, feel free to contact us! Likewise, if our vocabulary is offensive, feel free to contact us to correct us.

Here is a non-exhaustive list of implemented features to make your life easier:
- Color palette has been chosen with color-blindness
- Visual impairness (WIP)
- Muscular weakness and Reduced mobility (WIP)

## Known issues
*As the application is still currently in development stage, the list of known issues is still long and we are actively working on reducing severity and amount of bugs!*
