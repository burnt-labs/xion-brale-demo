Pod::Spec.new do |s|
  s.name             = 'UncommonCrypto'
  s.version          = '999.99.9'
  s.summary          = 'Swift wrapper over CommonCrypto with Linux support.'

  s.description      = <<-DESC
  Swift wrapper over CommonCrypto with alternative C implementation for Linux support.
                       DESC

  s.homepage         = 'https://github.com/tesseract-one/UncommonCrypto.swift'

  s.license          = { :type => 'Apache 2.0', :file => 'LICENSE' }
  s.author           = { 'Tesseract Systems, Inc.' => 'info@tesseract.one' }
  s.source           = { :git => 'https://github.com/tesseract-one/UncommonCrypto.swift.git', :tag => s.version.to_s }

  s.swift_version    = '5.4'

  base_platforms     = { :ios => '11.0', :osx => '10.13', :tvos => '11.0' }
  s.platforms        = base_platforms.merge({ :watchos => '6.0' })

  s.module_name      = 'UncommonCrypto'

  s.source_files = 'Sources/UncommonCrypto/*.swift', 'Sources/CUncommonCrypto/**/*.{h,c}'
  s.public_header_files = 'Sources/CUncommonCrypto/include/*.h'

  s.test_spec 'Tests' do |ts|
    ts.platforms = base_platforms
    ts.source_files = 'Tests/UncommonCryptoTests/**/*.swift'
  end
end
