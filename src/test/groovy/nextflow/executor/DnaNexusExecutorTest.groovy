package nextflow.executor

import com.dnanexus.DXJSON
import nextflow.exception.MissingFileException
import nextflow.processor.TaskConfig
import nextflow.util.DxFile
import spock.lang.Specification
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
class DnaNexusExecutorTest extends Specification {

    def 'test outputs' () {

        setup:
        def outStr = '''
                   {
                      "file1":"file-Z0C09V9F9WJQ7FJRKFKLLDFG",
                      "file2":"file-7F7G7G7S7HTKGLH0D9A8A8D8",
                      "file_x.fa":"file-B7vV85J5J6LLJ33K5LL6N4LY",
                      "file_y.fa":"file-B7vVF9G99GFMDK5KDNDJFY5Y",
                      "file_z.fa":"file-7FJFJ5J6SLA9FGKS6DHF8RFJ",
                      "exit_code":"0"
                   }
                   '''

        def output = DXJSON.parseJson(outStr)
        def executor = new DnaNexusExecutor()
        executor.taskConfig = new TaskConfig([name:'tesk0'])

        // a single file is specification
        when:
        def file0 = executor.getFiles( output, 'file_z.fa' )
        then:
        file0 == new DxFile(name:'file_z.fa', id:'file-7FJFJ5J6SLA9FGKS6DHF8RFJ')


        // question mark wildcard is used
        when:
        def files1 = executor.getFiles( output, 'file_*' )

        then:
        files1 == [
                new DxFile(name:'file_x.fa', id:'file-B7vV85J5J6LLJ33K5LL6N4LY'),
                new DxFile(name:'file_y.fa', id:'file-B7vVF9G99GFMDK5KDNDJFY5Y'),
                new DxFile(name:'file_z.fa', id:'file-7FJFJ5J6SLA9FGKS6DHF8RFJ')
        ]


        // star wildcard used, returns the matching list of DxFile(s)
        when:
        def files2 = executor.getFiles( output, 'file?' )
        then:
        files2 == [
                new DxFile(name:'file1', id:'file-Z0C09V9F9WJQ7FJRKFKLLDFG'),
                new DxFile(name:'file2', id:'file-7F7G7G7S7HTKGLH0D9A8A8D8')
        ]


        // missing file
        when:
        executor.getFiles(output,'missing.file')
        then:
        thrown(MissingFileException)


        // empty list
        when:
        def listEmpty = executor.getFiles(output, '*.gtf')
        then:
        thrown(MissingFileException)



    }


    def testRealOutput() {

        setup:
        def str = '''
            {".command.out":"file-B7xxj1j0j589v1JqPk7VP96X","exit_code":"0","seq_04":"file-B7xxj3j0j58B5k7YPJ4VZ8PP","seq_02":"file-B7xxj300j58B5k7YPJ4VZ8KJ","seq_03":"file-B7xxj3Q0j589v1JqPk7VP992","seq_00":"file-B7xxj200j589v1JqPk7VP96j","seq_01":"file-B7xxj2Q0j586f59yQV7Q6Gpp"}
            '''
        def output = DXJSON.parseJson(str)
        def executor = new DnaNexusExecutor()
        executor.taskConfig = new TaskConfig([name:'task-1'])

        when:
        def result = executor.getFiles(output, 'seq_02')

        then:
        output.isObject()
        result == new DxFile( name: 'seq_02', id: 'file-B7xxj300j58B5k7YPJ4VZ8KJ' )


    }

}
