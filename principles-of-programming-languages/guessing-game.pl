% Knowledge base

:- dynamic positiveFact/1.
:- dynamic negativeFact/1.

clearFacts :- retractall(positiveFact(Question)), retractall(negativeFact(Question)).

isTrue(Question) :-
  (
    positiveFact(Question) ->
      true ;
      (
        negativeFact(Question) ->
          false ;
          format('~w?~n', [Question]),
          (
            read(yes) ->
              assert(positiveFact(Question)), true ;
              assert(negativeFact(Question)), false
          )
      )
  ).

game('Rocket League') :-
  (
    isTrue('Is the genre Racing'),
    (
      isTrue('Is it published by Psyonix') ;
      isTrue('Is it published by Epic Games')
    )
  ).
game('Mario Kart') :-
  (
    isTrue('Is the genre Racing'),
    isTrue('Is it published by Nintendo')
  ).
game('Call of Duty') :-
  (
    isTrue('Is the genre First-Person Shooter'),
    isTrue('Is it published by Activision')
  ).
game('Apex Legends') :- 
  (
    (
      isTrue('Is the genre Battle Royale') ;
      isTrue('Is the genre First-Person Shooter')
    ),
    isTrue('Is it published by Electronic Arts')
  ).
game('Overwatch') :- 
  (
    isTrue('Is the genre First-Person Shooter'),
    isTrue('Is it published by Blizzard Entertainment')
  ).
game('Minecraft') :- 
  (
    (
      isTrue('Is the genre Sandbox') ;
      isTrue('Is the genre Survival') ;
      isTrue('Is the genre Action-Adventure')
    ),
    (
      isTrue('Is it published by Mojang Studios') ;
      isTrue('Is it published by Telltale Games') ;
      isTrue('Is it published by Xbox Game Studios') ;
      isTrue('Is it published by Sony Interactive Entertainment')
    )
  ).
game('Grand Theft Auto') :-
  (
    isTrue('Is the genre Action-Adventure'),
    isTrue('Is it published by Rockstar Games')
  ).
game('Spider-Man') :-
  (
    isTrue('Is the genre Action-Adventure'),
    isTrue('Is it published by Sony Interactive Entertainment'),
    isTrue('Is it based on a Comic Book Character')
  ).
game('Fortnite') :- 
  (
    (
      isTrue('Is the genre Battle Royale') ;
      isTrue('Is the genre Survival') ;
      isTrue('Is the genre Sandbox')
    ),
    (
      isTrue('Is it published by Epic Games') ;
      isTrue('Is it published by Warner Bros. Interactive Entertainment')
    )
  ).
game('Fall Guys') :- 
  (
    isTrue('Is the genre Battle Royale'),
    isTrue('Is it published by Devolver Digital')
  ).
game('Among Us') :-
  (
    isTrue('Is the genre Social Deduction'),
    isTrue('Is it published by InnerSloth')
  ).
game('League of Legends') :-
  (
    isTrue('Is the genre Multiplayer Online Battle Arena'),
    isTrue('Is it published by Riot Games')
  ).
game('Smite') :-
  (
    isTrue('Is the genre Multiplayer Online Battle Arena'),
    isTrue('Is it published by Hi-Rez Studios')
  ).
game('World of Warcraft') :- 
  (
    isTrue('Is the genre Massively Multiplayer Online Role-Playing'),
    isTrue('Is it published by Blizzard Entertainment')
  ).
game('RuneScape') :- 
  (
    isTrue('Is the genre Massively Multiplayer Online Role-Playing'),
    isTrue('Is it published by Jagex')
  ).

% User Interface 

begin :-
  clearFacts,
  write('Welcome to this ES about games!'), nl,
  write('I am going to ask questions about game features.'), nl,
  write('Please answer yes. or no.'), nl,
  write('Ready?'), nl,
  (
    read(yes) ->
      (
        game(GuessedGame) ->
          format('~w ~w.~n', ['I think your game is', GuessedGame]),
          write('Did I get it right?'), nl,
          (
            read(yes) ->
              write('nice') ;
              write('Not my fault! My designer did not give me enough information about games.')
          ) ;
          write('Hmm, I could not figure this one out...')
      ), nl,
      write('To try again, just type begin.') ;
      write('Bye!')
  ), nl.