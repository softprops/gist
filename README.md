# gist

gist: it's like git with an s between the i and t.

## usage

 Gist is a client of a github service. You will need a github login to do useful things like:
 
 Authenticate
 
    gist auth ghuser:ghpass
    
Answer an age old question

    gist whoami
    
Deauthenticate

    gist auth -d
    
List your gists

    gist ls
    
List list everyones gists

    gist ls -p
    
List a specific user's gists

    gist user :login
    
Show a gist

    gist show :id
    
Show a gists contents

    gist cat :id
    
Make a gist from arguments

    git -c 'some text' -n 'a name'
    
Make a gist from a stream

    cat file | gist -- -n 'a name'
    
Make a gist private

     gist - :id
     
Make a gist public

    gist + :id
    
Star a gist

    gist star :id
    
Unstart a gist

    gist start :id -d
    
Delete a gist

    gist rm :id
    
Doug Tangren (softprops) 2012
