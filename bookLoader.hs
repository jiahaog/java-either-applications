data Book = Book String deriving (Eq)

data BookLoadError

compareBooks :: Book -> Book -> Bool
compareBooks book1 book2 = book1 == book2

handleBookLoad :: Either Book BookLoadError -> Either Book BookLoadError -> (Either Book BookLoadError, Bool)
handleBookLoad (Left oldBook) (Left newBook) = (Left oldBook, compareBooks oldBook newBook)
handleBookLoad (Left oldBook) (Right _) = (Left oldBook, False)
handleBookLoad (Right oldLoaderError) (Left _) = (Right oldLoaderError, True)
handleBookLoad (Right oldLoaderError) (Right _) = (Right oldLoaderError, True)

handleBookLoad1 :: Either Book BookLoadError -> Either Book BookLoadError -> (Either Book BookLoadError, Bool)
handleBookLoad1 oldLoadResult newLoadResult = either
    (\oldBook ->
        either
            (\newBook -> (Left oldBook, compareBooks oldBook newBook))
            (\_ -> (Left oldBook, False))
            newLoadResult
    )
    (\oldLoaderError ->
        either
            (\_ -> (Right oldLoaderError, True))
            (\_ -> (Right oldLoaderError, True))
            newLoadResult
    )
    oldLoadResult