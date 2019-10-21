//Total Run Time on i9-9900k: 0.023 - 0.024 s 
//Total Run Time on i9-9900k: 0.024-0.032 s 
//Total Run Time on i9-9900k: 0.034-0.04 s 
//Total Run TIme on i9-9900k: 0.049-0.053 s
//Total Run Time on i9-9900k: 0.067-0.071 s



/**
20190925:
TEST VERSION 1  LOTS OF BUGS HERE : 1.Array out of bound 2.When adding and multiplying overflow

TEST VERSION 2 Calculate the checksum when reading into dictionary array. Reduced runtime by 0.02s (25%).
TEST VERSION 2.1 Calculate the checksum right after reading jString. Reduced runtime by 0.005s (3%).

TEST VERSION 3 Changed to BufferWriter and reduce the array length. Reduced runtime by 0.015s (25%)

TEST VERSION 4 Implement a simpler file reader, returning chars instead of string. CharAt() method has been reduced. Reduced runtime by 0.015s (40%)

TEST VERSION 5  Changed from length-index relationship to sumMod-index relationship. Reduced runtime by 0.004.

BUGS (if increase dictionary file length or word length): 1.ARRAY OUT OF BOUND 2.overflow.
*/





import java.util.*;
import java.io.*;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;

public class Jumbles {
	
	private static final int SIZE = 4096;
	private static final int WORD_MAP_SIZE = 256;
	
    public static void main(String[] args) throws Exception {
		
		long startTime = System.nanoTime();
		
		final int MAX_WORD_LENGTH  = 50;
		final int ESTIMATE_dWORD_COUNT  = 180000;
		final int ESTIMATE_jWORD_COUNT = 60;
		
		
		
		
		if (args.length < 2) 
		{
			System.out.println("\nPlease enter 2 files");
			System.exit(0);
		}
		
		
		
		File dFile = new File(args[0]);
		if(!dFile.exists()) throw new RuntimeException("File Does Not Exist");
		FileInputStream dFis = new FileInputStream(dFile);
	
		//BufferedReader dFile = new BufferedReader(new FileReader(args[0]));
        BufferedReader jFile = new BufferedReader(new FileReader(args[1]));
		 

		BufferedWriter myBufferWriter = new BufferedWriter(new OutputStreamWriter(System.out),256);

		//TODO: The wordCountMap may out of the bound. SOLVE IT!!!!! BUT I DON'T KNOW HOW TO UPSIZE 2D ARRAY.
		//REMEMBER: NEVER USE wordCountMap[Len][0], BECAUSE IT IS RESERVED AS COUNT INDICATOR.
		//WHEN TRAVERSING, START FROM wordCountMap[Len][1].
		int[][] wordCountMap = new int[WORD_MAP_SIZE][1000];   //Length-index map. Where the first dimension stores the length, the second dimension stores the index that = the length.
		
		
		
		int dictionaryCount = 0; //Numbers of words in dicionary
		
		String[] dictionaryList = new String[ESTIMATE_dWORD_COUNT]; //Stores the word for dicitonary file
		int[] addCheckSumList = new int[ESTIMATE_dWORD_COUNT]; //Stores the sum for ascii in dictionary file for the further comparison.
		int[] multiplyCheckSumList = new int[ESTIMATE_dWORD_COUNT]; //Stores the product for ascii in dictionary file for the further comparison.
	
		
				
		
		int readLen = 0; //size of data readed into RAM. Equals to the buffer size. TODO: Is 4096 Fastest?
		byte[] buf = new byte[SIZE]; //Buffer
		
		int lengthCount = 0, asciiSum = 0, asciiProduct = 1; //lengthCount: tempBuffer for counting the length of the word. asciiSum: tempBuffer for storing the ascii sum of the word. asciiProduct: tempBuffer for storing the ascii product of the word.
		
		char[] tempCharBuffer = new char[MAX_WORD_LENGTH]; //tempBuffer to store the char that needed to be convert to string.
		
	
		
		String[] jumbleList = new String[ESTIMATE_jWORD_COUNT];
		int jumbleCount = 0;
		
		int wordCountMapIndex = 0;
		
		/**
		The simplest implementation of file reader. 
		Bufferreader.readLine() cost 1/2 of the runtime. It returns String, by calling .charAt() to get ASCII is tooooooo slow.
		
		Variable: lengthCount, asciiSum, asciiProduct
		Using FileInputStream, the code below do the following things:
		1. Read each letter to buffer.
		2. If the letter is not 13 or 10 (enter or \n), add the number to asciiSum. Multiply the number to asciiProduct. Append the letter to tempCharBuffer, waiting for convertion to String.
		3. If the letter is 13 or 10. Stores the asciiSum, asciiProduct, String to the array. Add the length-index to the length-index map array, increase the count of that array. And recover the counter and the variables.
			Because Sometimes  \n and enter appear the same time (I don't know why, different from files to files). If wordCount == 0, that means the storing operation has already been made, so just continue.
		4. After reading all the chars and converting them, check if the length != 0. Because sometimes the last ascii of the file is not 13 or 10.
		*/
		while( ( readLen = dFis.read( buf ) ) != -1 )
		{
			
			for ( int i = 0 ; i < readLen ; i++)
			{
				
				if ( buf[i] == 13 || buf[i] == 10 ) //\n or enter
				{
					
					if ( lengthCount == 0 ) //0 means already converted into array. Because sometimes 13 and 10 is toghter;
						continue;
						
					if ( dictionaryCount == dictionaryList.length )	 //upsize
					{
						dictionaryList = upSizeArr(dictionaryList);
						addCheckSumList = upSizeArr(addCheckSumList);
						multiplyCheckSumList = upSizeArr(multiplyCheckSumList);
					}
					
					dictionaryList[dictionaryCount] = new String( tempCharBuffer , 0 , lengthCount ); //convert the char[] to String, and stores it into dictionaryList
					
					//TWO DIMENTIONAL ARRAY NEED TO BE UPSIZED. BUT I DON'T KNOW HOW.
					// if(wordCountMap.outOfBound == 1)
						//wordCountMap.upsize();
					wordCountMapIndex = asciiSum%WORD_MAP_SIZE;
					
					wordCountMap[wordCountMapIndex][ wordCountMap[wordCountMapIndex][0] + 1 ] = dictionaryCount + 1; //UPSIZE WHEN NEEDED
					wordCountMap[wordCountMapIndex][0]++; //wordCountMap[0] as counter
					
					
					addCheckSumList[dictionaryCount] = asciiSum; //Store the sum value to sum Array
					multiplyCheckSumList[dictionaryCount] = asciiProduct; //Store the product value to product array
					
					//recover variable
					lengthCount = 0; 
					asciiSum = 0;
					asciiProduct = 1;
					dictionaryCount++; //count+1
					continue;
				}
				
				tempCharBuffer[lengthCount] = (char)buf[i]; //UPSIZE WHEN NEEDED
				
				asciiSum += buf[i];
				asciiProduct *= buf[i];
				
				lengthCount++;
			}
			
			
		}
		
		//if there is no \n or enter at the end of the file
		if ( lengthCount != 0 )
		{
			
			if ( dictionaryCount == dictionaryList.length )	
			{
				
				dictionaryList = upSizeArr(dictionaryList);
				addCheckSumList = upSizeArr(addCheckSumList);
				multiplyCheckSumList = upSizeArr(multiplyCheckSumList);
				
			}
				
			dictionaryList[dictionaryCount] = new String( tempCharBuffer , 0 , lengthCount );
			
			//TWO DIMENTIONAL ARRAY NEED TO BE UPSIZED. BUT I DON'T KNOW HOW.
			// if(wordCountMap.outOfBound == 1)
				//wordCountMap.upsize();
			wordCountMapIndex = asciiSum%WORD_MAP_SIZE;
			
			wordCountMap[wordCountMapIndex][ wordCountMap[wordCountMapIndex][0] + 1 ] = dictionaryCount + 1; //UPSIZE WHEN NEEDED
			wordCountMap[wordCountMapIndex][0]++; //wordCountMap[lengthCount][0] as counter
			
			
			addCheckSumList[dictionaryCount] = asciiSum;
			multiplyCheckSumList[dictionaryCount] = asciiProduct;
			
			//recover
			lengthCount = 0;
			asciiSum = 0;
			asciiProduct = 1;
			dictionaryCount++;
		}
		
		dFis.close();



		//read jumble file
		while (jFile.ready()) 
		{
			if ( jumbleList.length == jumbleCount )
			{
				jumbleList = upSizeArr(jumbleList);
			}
			jumbleList[jumbleCount++] = jFile.readLine();
		}
		
		
		
		//Arrays.sort(dictionaryList, 0, dictionaryCount); sort this one after finding
		Arrays.sort(jumbleList, 0, jumbleCount); 

		
		
		int jLen; //The length of the jumble string
		int[] compareIndexList; //The possible index array that can be equivalent to the jumble string
		int jCount = 0;//Indicates the length of the possible index array.
		
		String jString; //Jumble string
		
		String[] tempBuffer = new String[10]; //tempBuffer to store the equivalent string
		int tempBufferCount = 0; //Indicates the count of tempBuffer. jCount 
		int addCheckSumjString = 0,  multiplyCheckSumjString = 0; //tempBuffer for the jstring ascii sum and product.

		for (int i =0;i<jumbleCount;i++) 
		{
			jString = jumbleList[i]; //Retrive the jString
			
			jLen = jString.length(); //Get the length
			
			
			
			addCheckSumjString = getAdditionASCII(jString);
			multiplyCheckSumjString = getMultiplyASCII(jString);
			
			wordCountMapIndex = addCheckSumjString%WORD_MAP_SIZE;
			
			compareIndexList = wordCountMap[wordCountMapIndex]; //Retrive the possible index array. TODO: SOLVE BUGS: If the jstring length is too much, this will out of bound.
			jCount = wordCountMap[wordCountMapIndex][0]; //Retrive the possible index array length.
			
			for( int j = 1 ; j <= jCount ; j++ ) //1?
			{
				
				// if (0 == compareIndexList[j]) //need to be changed (it's a direct check of array content). But I can't understand what I wrote after one day!!!!
				// {
					// break;
				// }
				if (addCheckSumList[compareIndexList[j]-1] == addCheckSumjString && multiplyCheckSumList[compareIndexList[j]-1] == multiplyCheckSumjString)
				{					
					if (tempBufferCount == tempBuffer.length) tempBuffer = upSizeArr(tempBuffer);
					
					tempBuffer[tempBufferCount++] = dictionaryList[compareIndexList[j]-1]; //If the sum and product are equal, add them to tempBuffer.
				}
			}
			if (tempBufferCount>1) //Only sort when there is more than one word.
			{
				Arrays.sort(tempBuffer, 0, tempBufferCount);
			}
			
			
			myBufferWriter.write(jString);
			myBufferWriter.write(" ");
			
			for (int c = 0; c < tempBufferCount;c++)
			{
				myBufferWriter.write(tempBuffer[c]);
				myBufferWriter.write(" ");
			}
			
			myBufferWriter.write("\n");
			
			tempBufferCount = 0; //New loop start, set the count to 0.
			
		}
		myBufferWriter.flush();
		
		long endTime = System.nanoTime();
        long ms = endTime - startTime;
        System.out.println("Elapsed time in seconds: " + ms / 1000000000.0  + "\n");
		 
	}
	
	static int getAdditionASCII(String s)
	{
		int a = 0;
		for( int i=0, length = s.length() ; i < length ; i++ )
		{ 
			a += s.charAt(i);
		}
		return a;
	}
	
	static int getMultiplyASCII(String s)
	{
		int a = 1;
		for( int i=0, length = s.length() ; i < length ; i++ )
		{ 
			a *= s.charAt(i);
		}
		return a;
	}
	
	static String[] upSizeArr( String[] fullArr )
	{
		String[] upSizedArr = new String[ fullArr.length * 2 ];
		
		System.arraycopy(fullArr, 0, upSizedArr, 0, fullArr.length); 
		
		return upSizedArr;
	}
	
	static int[] upSizeArr( int[] fullArr )
	{
		int[] upSizedArr = new int[ fullArr.length * 2 ];
		
		System.arraycopy(fullArr, 0, upSizedArr, 0, fullArr.length); 

		return upSizedArr;
	}
	
	static int[] upSizeArr( int[] fullArr , int length)
	{
		int[] upSizedArr = new int[ length ];
		
		System.arraycopy(fullArr, 0, upSizedArr, 0, fullArr.length); 
		
		return upSizedArr;
	}
	
}